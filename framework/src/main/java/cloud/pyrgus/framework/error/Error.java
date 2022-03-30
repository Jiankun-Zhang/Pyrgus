/*
 * Copyright (c) 2022. Zhang Jiankun
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
 *
 */

package cloud.pyrgus.framework.error;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/30
 */
public interface Error {

    static ClientError clientError(ErrorEnum errorEnum, String target) {
        return clientError(errorEnum, target, null);
    }

    static ClientError clientError(ErrorEnum errorEnum, String target, InnerError innerError, Error... details) {
        return clientError(errorEnum, target, Arrays.asList(details), innerError);
    }

    static ClientError clientError(ErrorEnum errorEnum, String target, List<Error> details, InnerError innerError) {
        return new ClientError(errorEnum.getCode(), errorEnum.getMessage(), target, details, innerError);
    }

    static ServerFault serverFault(ErrorEnum errorEnum, String target) {
        return serverFault(errorEnum, target, null);
    }

    static ServerFault serverFault(ErrorEnum errorEnum, String target, InnerError innerError, Error... details) {
        return serverFault(errorEnum, target, Arrays.asList(details), innerError);
    }

    static ServerFault serverFault(ErrorEnum errorEnum, String target, List<Error> details, InnerError innerError) {
        return new ServerFault(errorEnum.getCode(), errorEnum.getMessage(), target, details, innerError);
    }

    String getCode();

    default String getType() {
        return this.getClass().getSimpleName();
    }

    String getMessage();

    String getTarget();

    List<Error> getDetails();

    InnerError getInnerError();

}
