package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.filter.*;
import cn.zcn.rpc.bootstrap.utils.MethodSignatureUtil;
import cn.zcn.rpc.remoting.InvocationContext;
import cn.zcn.rpc.remoting.RequestHandler;
import cn.zcn.rpc.remoting.protocol.RpcStatus;
import io.netty.util.concurrent.Future;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC 请求处理器，用于处理 {@code RpcRequest}。
 *
 * <p>解析 {@code ProviderInterfaceConfig} 获取服务端注册的 RPC 接口和接口的方法。
 *
 * <p>当请求到来时，根据 {@code RpcRequest} 中的接口信息匹配接口实例，并处理请求。
 *
 * @author zicung
 */
public class ProviderRequestHandler implements RequestHandler<RpcRequest> {

    private static final class RegisteredInterface {
        private Object instance;
        private FilterChain<ProviderInvocation> filterChain;
        private final Map<String, RegisteredMethod> methods = new HashMap<>();
    }

    private static final class RegisteredMethod {
        private Method method;
    }

    private final ProviderConfig providerConfig;
    private final Supplier<Boolean> isServerStarted;
    private final ReflectInvocationFilter reflectInvocationFilter = new ReflectInvocationFilter();
    private final Map<String, RegisteredInterface> registeredInterfaces = new HashMap<>();

    public ProviderRequestHandler(ProviderConfig providerConfig, Supplier<Boolean> isServerStarted) {
        this.providerConfig = providerConfig;
        this.isServerStarted = isServerStarted;
    }

    /** 解析服务提供者注册的接口，获取服务端注册的所有的接口信息 */
    public void resolve() {
        for (ProviderInterfaceConfig config : providerConfig.getInterfaceConfigs()) {
            RegisteredInterface registeredInterface = new RegisteredInterface();
            registeredInterface.instance = config.getImpl();

            Class<?> clazz = config.getInterfaceClass();
            for (Method method : clazz.getMethods()) {
                if (method.isDefault() || Modifier.isStatic(method.getModifiers())) {
                    // jdk1.8, default methods in interface, static methods in interface
                    continue;
                }

                RegisteredMethod registeredMethod = new RegisteredMethod();
                registeredMethod.method = method;

                registeredInterface.methods.put(MethodSignatureUtil.getMethodSignature(method), registeredMethod);
            }

            FilterChain<ProviderInvocation> filterChain = FilterChainBuilder
                .buildProviderFilterChain(config.getFilters());
            filterChain.addLast(reflectInvocationFilter);
            registeredInterface.filterChain = filterChain;

            registeredInterfaces.put(config.getInterfaceClass().getName(), registeredInterface);
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
    public void handle(InvocationContext ctx, RpcRequest request) {
        RpcResponse response = new RpcResponse();
        if (!isServerStarted.get()) {
            response.setException(new RpcException("ProviderBootstrap was stopped."));
            ctx.writeAndFlushResponse(response);
            return;
        }

        String clazz = request.getClazz();
        RegisteredInterface registeredInterface = registeredInterfaces.get(clazz);
        if (registeredInterface == null) {
            response.setException(new RpcException("Interface can not be found. Interface:{}", clazz));
            ctx.writeAndFlushResponse(response);
            return;
        }

        String methodSignature = MethodSignatureUtil.getMethodSignature(request.getMethodName(),
            request.getParameterTypes());
        RegisteredMethod registeredMethod = registeredInterface.methods.get(methodSignature);
        if (registeredMethod == null) {
            response.setException(new RpcException("Method can not be found. Method:{}", methodSignature));
            ctx.writeAndFlushResponse(response);
            return;
        }

        ProviderInvocation invocation = new ProviderInvocation(request);
        invocation.setInvocationContext(ctx);
        invocation.setInstance(registeredInterface.instance);
        invocation.setMethod(registeredMethod.method);

        registeredInterface.filterChain.doFilter(invocation);
    }

    private static class ReflectInvocationFilter implements ProviderFilter {

        @Override
        public void doFilter(ProviderInvocation invocation, FilterContext<ProviderInvocation> context)
            throws RpcException {
            RpcResponse response = invocation.getResponse();
            InvocationContext ctx = invocation.getInvocationContext();

            try {
                Object result = invocation.getMethod()
                    .invoke(invocation.getInstance(), invocation.getRequest().getParameters());
                response.set(result);
                ctx.writeAndFlushResponse(response);

                context.doFilter(invocation);
            } catch (Throwable t) {
                response.setException(t.getCause());
                ctx.writeAndFlushResponse(response, RpcStatus.SERVICE_ERROR);
            }
        }
    }
}
