package cn.zcn.rpc.remoting.config;

/**
 * {@code Option} 集合，提供获取选项值的能力。
 *
 * @author zicung
 */
public interface Options {

    /**
     * 获取选项值。
     *
     * @param option 选项
     * @param <T>    选项值的类型
     * @return 如果选项存在返回选项值；如果选项不存在返回 {@code Option} 的默认值。
     */
    <T> T getOption(Option<T> option);
}
