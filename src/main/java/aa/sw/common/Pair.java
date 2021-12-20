package aa.sw.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import static java.util.Objects.requireNonNull;

@Value
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Pair<A, B> {

    A left;
    B right;

    public static <L,R> Pair<L,R> of(final L left, final R right){
        requireNonNull(left);
        requireNonNull(right);

        return new Pair<>(left, right);
    }
}
