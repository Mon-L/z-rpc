package cn.zcn.rpc.remoting.config;

public interface Options {
    <T> T getOption(Option<T> option);
}
