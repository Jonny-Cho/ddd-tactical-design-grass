package kitchenpos.products.tobe.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DisplayedNameTest {

    @DisplayName("빈 이름으로 DisplayedName을 생성할 수 없다")
    @ParameterizedTest
    @NullAndEmptySource
    void wrongName(final String name) {
        assertThatThrownBy(
            () -> new DisplayedName(name)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("올바른 이름으로 DisplayedName을 생성할 수 있다")
    @ParameterizedTest
    @ValueSource(strings = {"상품1", "상품2", "상품3"})
    void name(final String name) {
        final DisplayedName displayedName = new DisplayedName(name);

        assertThat(displayedName).isNotNull();
    }

    @DisplayName("name이 같으면 equals의 결과도 같다")
    @ParameterizedTest
    @ValueSource(strings = {"상품1", "상품2", "상품3"})
    void equals(final String name) {
        final DisplayedName displayedName1 = new DisplayedName(name);
        final DisplayedName displayedName2 = new DisplayedName(name);

        assertThat(displayedName1.equals(displayedName2)).isTrue();
    }

}