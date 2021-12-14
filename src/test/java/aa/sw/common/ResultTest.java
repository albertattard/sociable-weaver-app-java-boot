package aa.sw.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void returnAnErrorWhenTheSupplierFails() {
        /* Given */
        final Exception exception = new Exception("Simulating error");
        final Result.ResultSupplier<String> supplier = () -> { throw exception; };

        /* When */
        final Result<String> result = Result.of(supplier);

        /* Then */
        assertThat(result)
                .isEqualTo(Result.error(exception));
    }
}
