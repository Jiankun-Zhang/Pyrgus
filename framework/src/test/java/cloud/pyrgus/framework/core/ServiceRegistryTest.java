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

package cloud.pyrgus.framework.core;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.configuration.Configuration;
import cloud.pyrgus.framework.core.configuration.PropertiesConfiguration;
import cloud.pyrgus.framework.core.message.*;
import cloud.pyrgus.framework.core.service.EmptyService;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.service.exception.MultipleServiceImplementationFoundException;
import cloud.pyrgus.framework.core.service.exception.ServiceImplementationNotFoundException;
import cloud.pyrgus.framework.core.service.exception.ServiceRegistryAlreadyConfiguredException;
import cloud.pyrgus.framework.core.service.exception.ServiceRegistryNotInitializedException;
import cloud.pyrgus.framework.internal.simple.core.SimpleCoreServiceRegistry;
import cloud.pyrgus.framework.internal.simple.core.message.SimpleMessageAdapter;
import cloud.pyrgus.framework.internal.simple.core.message.SimpleMessageRouter;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageFilteringFailedException;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageHandlerNotFoundException;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageInboxUndefinedException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
class ServiceRegistryTest {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    private final String HANDLER_FOO = "Foo";

    @SneakyThrows
    @BeforeEach
    void beforeEach() {
        resetInstance(Pyrgus.class.getDeclaredField("instance"));
        resetInstance(SimpleCoreServiceRegistry.class.getDeclaredField("instance"));
    }

    @SneakyThrows
    private void resetInstance(Field field) {
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void should_throw_when_service_registry_is_not_initialized() {
        assertThatThrownBy(Pyrgus::getServiceRegistry)
                .isInstanceOf(ServiceRegistryNotInitializedException.class);

        assertThatThrownBy(Pyrgus::getConfiguration)
                .isInstanceOf(ServiceRegistryNotInitializedException.class);
    }

    @Test
    void should_throw_when_build_pyrgus_service_registry_twice() {
        SimpleCoreServiceRegistry.builder().build();
        assertThatThrownBy(() -> SimpleCoreServiceRegistry.builder().build())
                .isInstanceOf(ServiceRegistryAlreadyConfiguredException.class);
    }

    @Test
    void should_return_none_when_service_type_not_registered() {
        initializedPyrgus();
        assertThat(Pyrgus.getServiceRegistry().loadService(EmptyService.class))
                .isEmpty();
    }

    @Test
    void should_throw_when_getting_service_not_registered() {
        initializedPyrgus();
        assertThatThrownBy(() -> Pyrgus.getServiceRegistry().loadServiceOrThrown(EmptyService.class))
                .isInstanceOf(ServiceImplementationNotFoundException.class);
    }

    @Test
    void should_throw_when_loading_one_instance_of_service_that_implemented_multiple_times() {
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(EmptyService.class, new EmptyService() {
                })
                .applyService(EmptyService.class, new EmptyService() {
                })
                .build());
        assertThatThrownBy(() -> Pyrgus.getServiceRegistry().loadService(EmptyService.class))
                .isInstanceOf(MultipleServiceImplementationFoundException.class);
    }

    @Test
    void should_load_specified_one_by_configuration_with_multiple_implementation() {
        EmptyService service1 = new EmptyService() {
        };
        EmptyService service2 = new EmptyService() {
        };
        Properties properties = new Properties();
        properties.put(EmptyService.class.getName(), service2.getClass().getName());
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(EmptyService.class, service1)
                .applyService(EmptyService.class, service2)
                .build(), new PropertiesConfiguration(properties));
        assertThat(Pyrgus.getServiceRegistry().loadService(EmptyService.class))
                .containsExactly(service2);
    }

    @Test
    void should_get_value_from_system_properties() {
        Properties properties = System.getProperties();
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .build(), new PropertiesConfiguration(properties));
        String propertyKey = "user.dir";
        assertThat((String) (Pyrgus.getConfiguration()).get(propertyKey)).isEqualTo(System.getProperty(propertyKey));
    }

    @Test
    void should_get_null_with_not_exists_key() {
        initializedPyrgus();
        assertThat((Object) Pyrgus.getConfiguration().get("test")).isNull();
    }

    @Test
    void should_override_simple_message_factory() {
        MessageFactory messageFactory = (payload, headers) -> null;
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(MessageFactory.class, messageFactory)
                .build());
        assertThat(Pyrgus.getServiceRegistry().loadService(MessageFactory.class))
                .isNotEmpty()
                .containsExactly(messageFactory);
    }

    @Test
    void should_throw_when_no_handler_matches() {
        initializedPyrgus();

        assertThatThrownBy(() -> sendToFoo(1L).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(MessageHandlerNotFoundException.class);
    }

    @Test
    void should_throw_when_message_does_not_have_inbox() {
        initializedPyrgus();
        assertThatThrownBy(() -> getSimpleMessageAdapter().read(1L, null).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(MessageInboxUndefinedException.class);
    }

    @Test
    void should_plus_one_by_handler() throws ExecutionException, InterruptedException {
        initializedPyrgus();
        registerFooHandler(justPlusOneThenCastToString());

        CompletableFuture<String> future = sendToFoo(1L);
        assertThat(future).isCompleted();
        assertThat(future.get()).isEqualTo("2");
    }

    @Test
    void should_throw_when_filtering_failed() {
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(MessageFilter.class, new MessageFilter() {
                    @Override
                    public boolean doFilter(Message message) {
                        return message.getPayload() instanceof Long;
                    }

                    @Override
                    public int getOrder() {
                        return 0;
                    }
                })
                .build());

        registerFooHandler(justPlusOneThenCastToString());

        assertThatThrownBy(() -> getSimpleMessageAdapter().read("1", null).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(MessageFilteringFailedException.class);
    }

    @Test
    void should_throw_when_filtering_throws_exception() {
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(MessageFilter.class, new MessageFilter() {
                    @Override
                    public boolean doFilter(Message message) {
                        throw new IllegalArgumentException();
                    }

                    @Override
                    public int getOrder() {
                        return 0;
                    }
                })
                .build());

        registerFooHandler(justPlusOneThenCastToString());

        assertThatThrownBy(() -> getSimpleMessageAdapter().read("1", null).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(MessageFilteringFailedException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_when_intercepting_failed() {
        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(MessageInterceptor.class, new MessageInterceptor() {
                    @Override
                    public Object intercept(Message message, MessageHandler messageHandler, MessageInterceptorChain chain) {
                        long payload = (long) message.getPayload();
                        if (payload < 5) {
                            throw new IllegalArgumentException();
                        }
                        return chain.next(message);
                    }

                    @Override
                    public int getOrder() {
                        return 0;
                    }
                })
                .build());
        registerFooHandler(justPlusOneThenCastToString());

        assertThatThrownBy(() -> sendToFoo(1L).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_sort_filters_by_order() {
        MessageFilter filter1 = new MessageFilter() {

            @Override
            public boolean doFilter(Message message) {
                return false;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
        MessageFilter filter2 = new MessageFilter() {

            @Override
            public boolean doFilter(Message message) {
                return false;
            }

            @Override
            public int getOrder() {
                return 1;
            }
        };
        MessageFilter filter3 = new MessageFilter() {

            @Override
            public boolean doFilter(Message message) {
                return false;
            }

            @Override
            public int getOrder() {
                return 2;
            }
        };

        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(MessageFilter.class, filter3)
                .applyService(MessageFilter.class, filter1)
                .applyService(MessageFilter.class, filter2)
                .build());

        List<MessageFilter> filters = Pyrgus.getServiceRegistry().loadAllServices(MessageFilter.class);

        assertThat(filters).size().isEqualTo(3);
        assertThat(filters).element(0).isEqualTo(filter1);
        assertThat(filters).element(1).isEqualTo(filter2);
        assertThat(filters).element(2).isEqualTo(filter3);
    }

    @Test
    void should_sort_filters_by_order_from_properties() {
        MessageFilter filter1 = new MessageFilter() {

            @Override
            public boolean doFilter(Message message) {
                return false;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
        MessageFilter filter2 = new MessageFilter() {

            @Override
            public boolean doFilter(Message message) {
                return false;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
        MessageFilter filter3 = new MessageFilter() {

            @Override
            public boolean doFilter(Message message) {
                return false;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };

        Properties properties = new Properties();
        properties.put(filter1.getClass().getName() + ".order", "3");
        properties.put(filter2.getClass().getName() + ".order", "2");
        properties.put(filter3.getClass().getName() + ".order", "1");

        initializedPyrgus(SimpleCoreServiceRegistry.builder()
                .applyService(MessageFilter.class, filter3)
                .applyService(MessageFilter.class, filter1)
                .applyService(MessageFilter.class, filter2)
                .build(), new PropertiesConfiguration(properties));

        List<MessageFilter> filters = Pyrgus.getServiceRegistry().loadAllServices(MessageFilter.class);

        assertThat(filters).size().isEqualTo(3);
        assertThat(filters).element(0).isEqualTo(filter3);
        assertThat(filters).element(1).isEqualTo(filter2);
        assertThat(filters).element(2).isEqualTo(filter1);
    }

    private void initializedPyrgus() {
        initializedPyrgus(SimpleCoreServiceRegistry.builder().build());
    }

    private void initializedPyrgus(ServiceRegistry serviceRegistry) {
        Pyrgus.builder()
                .withServiceRegistry(serviceRegistry)
                .build();
    }

    private void initializedPyrgus(ServiceRegistry serviceRegistry, Configuration configuration) {
        Pyrgus.builder()
                .withServiceRegistry(serviceRegistry)
                .withConfiguration(configuration)
                .build();
    }

    private <T extends Serializable> CompletableFuture<T> sendToFoo(Serializable payload) {
        Map<String, Serializable> headers = new HashMap<>();
        headers.put(SimpleMessageRouter.MSG_HEADER_INBOX, HANDLER_FOO);
        return getSimpleMessageAdapter().read(payload, headers);
    }

    private MessageHandler justPlusOneThenCastToString() {
        return (message, state) -> {
            long payload = (long) message.getPayload();
            return String.valueOf(payload + 1);
        };
    }

    private void registerFooHandler(MessageHandler handler) {
        ((SimpleMessageRouter) Pyrgus.getServiceRegistry().loadServiceOrThrown(MessageRouter.class))
                .addHandler(HANDLER_FOO, handler);
    }

    private MessageAdapter getSimpleMessageAdapter() {
        return Pyrgus.getServiceRegistry().loadServiceOrThrown(MessageAdapterRegistry.class)
                .getAdapterByName(SimpleMessageAdapter.NAME)
                .getOrElseThrow(() -> new RuntimeException("未加载 SimpleMessageAdapter !"));
    }

}