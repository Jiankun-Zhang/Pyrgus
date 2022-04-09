/*
 * MIT License
 *
 * Copyright (c) 2022 Zhang Jiankun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cloud.pyrgus.framework.core.component;

import cloud.pyrgus.framework.core.component.exception.NotSuchComponentException;
import cloud.pyrgus.framework.core.component.exception.NotUniqueComponentException;

import java.util.List;

/**
 * 注册表
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
public interface Registry {

    /**
     * 获得组件
     *
     * @param componentType 组件类型
     * @return <{@link C}> 返回该类型的已注册实例.
     * @throws NotUniqueComponentException 当指定的组件类型注册了不止一个实现时抛出此异常.
     * @throws NotSuchComponentException   当指定的组件类型没有注册实例时抛出此异常.
     */
    <C extends Component> C getComponent(Class<C> componentType);

    /**
     * 获得组件
     *
     * @param componentType 组件类型
     * @return {@link List}<{@link C}> 返回上下文中所有实现了指定组件类型的组件实例.
     */
    <C extends Component> List<C> getComponents(Class<C> componentType);

}
