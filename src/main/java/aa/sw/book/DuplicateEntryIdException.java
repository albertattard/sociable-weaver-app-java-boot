package aa.sw.book;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = false)
public class DuplicateEntryIdException extends RuntimeException {

    UUID id;
}
