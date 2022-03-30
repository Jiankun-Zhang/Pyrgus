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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.List;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/30
 */
@Value
public class ClientError implements Error {

    String code;
    String message;
    String target;
    List<Error> details;
    InnerError innerError;

    @Getter
    @AllArgsConstructor
    public enum Codes implements ErrorEnum {

        AuthFailure("400", "无法验证提供的凭据。您可能无权执行请求。"),
        Blocked("401", "您的账号目前已被封禁。如果您有任何问题请联系管理员。"),
        InvalidAction("404", "请求的操作无效，请检查操作标识。"),
        InvalidParameter("405", "参数中指定的值无效、不受支持或无法使用。"),
        BusinessOperationError("414", "业务操作失败。");

        private final String code;

        private final String message;

    }

}
