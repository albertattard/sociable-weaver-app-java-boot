package aa.sw.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static java.util.Objects.requireNonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Triple<L, M, R> {

    L left;
    M middle;
    R right;

    public static <L, M, R> Triple<L, M, R> of(final L left, final M middle, final R right) {
        requireNonNull(left);
        requireNonNull(middle);
        requireNonNull(right);

        return new Triple<>(left, middle, right);
    }
}
