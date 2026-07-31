package back.tesis.entities;

import jakarta.persistence.PrePersist;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class UuidEntityTest {

    static class Dummy extends UuidEntity {
    }

    @Test
    void assignsIdWhenNull() throws Exception {
        Dummy dummy = new Dummy();
        assertThat(dummy.getId()).isNull();

        Method assignId = UuidEntity.class.getDeclaredMethod("assignId");
        assignId.setAccessible(true);
        assignId.invoke(dummy);

        assertThat(dummy.getId()).isNotNull();

        assertThat(dummy.getId()).matches("[0-9a-f-]{36}");
    }

    @Test
    void doesNotOverwriteExistingId() throws Exception {
        Dummy dummy = new Dummy();
        dummy.id = "ya-existe";

        Method assignId = UuidEntity.class.getDeclaredMethod("assignId");
        assignId.setAccessible(true);
        assignId.invoke(dummy);

        assertThat(dummy.getId()).isEqualTo("ya-existe");
    }

    @Test
    void prePersistAnnotationIsPresent() throws Exception {
        Method assignId = UuidEntity.class.getDeclaredMethod("assignId");
        assertThat(assignId.isAnnotationPresent(PrePersist.class)).isTrue();
    }
}
