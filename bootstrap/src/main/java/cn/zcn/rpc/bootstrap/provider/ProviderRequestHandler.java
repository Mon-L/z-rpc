package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.utils.MethodSignatureUtil;
import cn.zcn.rpc.remoting.InvocationContext;
import cn.zcn.rpc.remoting.RequestHandler;
import io.netty.util.concurrent.Future;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * RPC 请求处理器
 */
public class ProviderRequestHandler implements RequestHandler<RpcRequest> {

    private static final class InterfaceMetadata {
        private Object instance;
        private final Map<String, Method> methods = new HashMap<>();
    }

    private final ProviderConfig providerConfig;
    private final Supplier<Boolean> isServerStarted;
    private final Map<String, InterfaceMetadata> interfaces = new HashMap<>();

    public ProviderRequestHandler(ProviderConfig providerConfig, Supplier<Boolean> isServerStarted) {
        this.providerConfig = providerConfig;
        this.isServerStarted = isServerStarted;
    }

    /**
     * 解析服务提供者注册的接口，获取可被调用的方法
     */
    public void resolve() {
        for (ProviderInterfaceConfig config : providerConfig.getInterfaceConfigs()) {
            InterfaceMetadata interMetadata = new InterfaceMetadata();
            interMetadata.instance = config.getImpl();

            Class<?> clazz = config.getInterfaceClass();
            for (Method m : clazz.getMethods()) {
                interMetadata.methods.put(MethodSignatureUtil.getMethodSignature(m), m);
            }

            interfaces.put(config.getInterfaceClass().getName(), interMetadata);
        }
    }

    @Override
    public String acceptableClass() {
        return RpcRequest.class.getName();
    }

    @Override
    public boolean ignoredTimeoutRequest() {
        return providerConfig.isIgnoreTimeoutRequest();
    }

    @Override
    public void run(InvocationContext ctx, RpcRequest request) {
        String clazz = request.getClazz();

        RpcResponse response = new RpcResponse();
        if (!isServerStarted.get()) {
            response.setException(new RpcException("ProviderBootstrap was stopped."));
            return;
        }

        InterfaceMetadata itfm = interfaces.get(clazz);
        if (itfm == null) {
            response.setException(new RpcException("Interface can not be found. Interface:{0}", clazz));
            ctx.writeAndFlushResponse(response);
            return;
        }

        String methodSignature = MethodSignatureUtil.getMethodSignature(request.getMethodName(), request.getParameterTypes());
        Method method = itfm.methods.get(methodSignature);
        if (method == null) {
            response.setException(new RpcException("Method can not be found. Method:{0}", methodSignature));
            ctx.writeAndFlushResponse(response);
            return;
        }

        doInvoke(ctx, request, response, itfm.instance, method);
    }

    /**
     * 调用业务处理方法
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void doInvoke(InvocationContext ctx, RpcRequest request, RpcResponse response, Object instance, Method method) {
        try {
            Object result = method.invoke(instance, request.getParameters());
            if (result instanceof Future) {
                //异步响应
                ((Future) result).addListener(future -> {
                    if (future.isSuccess()) {
                        response.set(result);
                    } else {
                        response.setException(future.cause());
                    }
                    ctx.writeAndFlushResponse(response);
                });
            } else {
                //同步响应
                response.set(result);
                ctx.writeAndFlushResponse(response);
            }
        } catch (Throwable t) {
            response.setException(t);
            ctx.writeAndFlushResponse(response);
        }
    }
}