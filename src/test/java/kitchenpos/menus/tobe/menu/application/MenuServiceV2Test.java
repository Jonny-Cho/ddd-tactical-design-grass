package kitchenpos.menus.tobe.menu.application;

import static kitchenpos.fixture.MenuFixture.MENU1;
import static kitchenpos.fixture.MenuFixture.NOT_DISPLAYED_MENU;
import static kitchenpos.fixture.MenuGroupFixture.MENU_GROUP1;
import static kitchenpos.fixture.ProductFixture.PRODUCT1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import kitchenpos.common.infra.Profanities;
import kitchenpos.common.tobe.FakeProfanities;
import kitchenpos.common.tobe.domain.DisplayedName;
import kitchenpos.menus.tobe.menu.domain.model.Menu;
import kitchenpos.menus.tobe.menu.domain.model.MenuPrice;
import kitchenpos.menus.tobe.menu.domain.repository.MenuRepository;
import kitchenpos.menus.tobe.menu.dto.MenuProductRequest;
import kitchenpos.menus.tobe.menu.dto.MenuRequest;
import kitchenpos.menus.tobe.menu.infra.MenuProductsMapper;
import kitchenpos.menus.tobe.menugroup.domain.repository.MenuGroupRepositoryV2;
import kitchenpos.products.tobe.application.TobeInMemoryProductRepository;
import kitchenpos.products.tobe.domain.model.Product;
import kitchenpos.products.tobe.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class MenuServiceV2Test {

    public static final UUID INVALID_ID = new UUID(0L, 0L);

    private MenuRepository menuRepository;
    private MenuGroupRepositoryV2 menuGroupRepository;
    private ProductRepository productRepository;
    private Profanities profanities;
    private MenuProductsMapper menuProductsMapper;
    private MenuServiceV2 menuServiceV2;
    private UUID menuGroupId;
    private Product product;

    @BeforeEach
    void setUp() {
        menuRepository = new TobeInMemoryMenuRepository();
        menuGroupRepository = new TobeInMemoryMenuGroupRepository();
        productRepository = new TobeInMemoryProductRepository();
        profanities = new FakeProfanities();
        menuProductsMapper = new MenuProductsMapper(productRepository);

        menuServiceV2 = new MenuServiceV2(menuRepository, menuGroupRepository, profanities, menuProductsMapper);
        menuGroupId = menuGroupRepository.save(MENU_GROUP1())
            .getId();
        product = productRepository.save(PRODUCT1());
    }

    @DisplayName("1??? ????????? ????????? ???????????? ????????? ????????? ??? ??????.")
    @Test
    void create() {
        final MenuRequest expected = new MenuRequest("????????????+????????????", BigDecimal.valueOf(19_000L), menuGroupId, true, new MenuProductRequest(product.getId(), 2L));

        final Menu actual = menuServiceV2.create(expected);

        assertThat(actual).isNotNull();
        assertAll(
            () -> assertThat(actual.getId()).isNotNull(),
            () -> assertThat(actual.getName()).isEqualTo(new DisplayedName(expected.getName(), profanities)),
            () -> assertThat(actual.getMenuPrice()).isEqualTo(new MenuPrice(expected.getPrice())),
            () -> assertThat(actual.getMenuGroupId()).isEqualTo(expected.getMenuGroupId()),
            () -> assertThat(actual.isDisplayed()).isEqualTo(expected.isDisplayed()),
            () -> assertThat(actual.getMenuProducts()
                .getMenuProducts()).hasSize(1)
        );
    }

    @DisplayName("????????? ????????? ????????? ??? ??????.")
    @Test
    void canNotCreateIfProductIsNotExist() {
        final MenuRequest expected = new MenuRequest("????????????+????????????", BigDecimal.valueOf(19_000L), menuGroupId, true, new MenuProductRequest(INVALID_ID, 2L));

        assertThatThrownBy(() -> menuServiceV2.create(expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ?????? ????????? ????????? 0??? ??????????????? ??????.")
    @Test
    void createNegativeQuantity() {
        final MenuRequest expected = new MenuRequest("????????????+????????????", BigDecimal.valueOf(19_000L), menuGroupId, true, new MenuProductRequest(product.getId(), -1L));

        assertThatThrownBy(() -> menuServiceV2.create(expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ????????? ???????????? ????????? ????????? ??? ??????.")
    @ValueSource(strings = {"-1000", "-1000000000000000"})
    @NullSource
    @ParameterizedTest
    void create(final BigDecimal price) {
        final MenuRequest expected = new MenuRequest("????????????+????????????", price, menuGroupId, true, new MenuProductRequest(product.getId(), 2L));

        assertThatThrownBy(() -> menuServiceV2.create(expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ?????? ?????? ????????? ?????? ????????? ???????????? ????????? ????????? ??????.")
    @Test
    void createExpensiveMenu() {
        final MenuRequest expected = new MenuRequest("????????????+????????????", BigDecimal.valueOf(190_000L), menuGroupId, true, new MenuProductRequest(product.getId(), 2L));

        final Menu menu = menuServiceV2.create(expected);

        assertThat(menu.isDisplayed()).isFalse();
    }

    @DisplayName("????????? ?????? ?????? ????????? ????????? ??????.")
    @Test
    void invalidMenuGroupId() {
        final MenuRequest expected = new MenuRequest("????????????+????????????", BigDecimal.valueOf(19_000L), INVALID_ID, true, new MenuProductRequest(product.getId(), 2L));

        assertThatThrownBy(() -> menuServiceV2.create(expected))
            .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("????????? ????????? ???????????? ????????? ????????? ??? ??????.")
    @ValueSource(strings = {"?????????", "????????? ????????? ??????"})
    @NullSource
    @ParameterizedTest
    void create(final String name) {
        final MenuRequest expected = new MenuRequest(name, BigDecimal.valueOf(19_000L), menuGroupId, true, new MenuProductRequest(product.getId(), 2L));

        assertThatThrownBy(() -> menuServiceV2.create(expected))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ????????? ????????? ??? ??????.")
    @Test
    void changePrice() {
        final Menu menu = MENU1();
        menuRepository.save(menu);
        final MenuPrice price = new MenuPrice(16_000L);
        final Menu actual = menuServiceV2.changePrice(menu.getId(), price);
        assertThat(actual.getMenuPrice()).isEqualTo(price);
    }

    @DisplayName("????????? ????????? ???????????? ????????? ????????? ??? ??????.")
    @ValueSource(strings = "-1000")
    @NullSource
    @ParameterizedTest
    void changePrice(final BigDecimal price) {
        assertThatThrownBy(() -> new MenuPrice(price))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("????????? ?????? ?????? ????????? ?????? ????????? ???????????? ????????? ????????? ??????.")
    @Test
    void changePriceExpensive() {
        final Menu menu = MENU1();
        menuRepository.save(menu);

        assertThat(menu.isDisplayed()).isTrue();

        final Menu changedMenu = menuServiceV2.changePrice(menu.getId(), new MenuPrice(100_000L));

        assertThat(changedMenu.isDisplayed()).isFalse();
    }

    @DisplayName("????????? ????????? ??? ??????.")
    @Test
    void display() {
        final Menu menu = MENU1();
        menuRepository.save(menu);
        final Menu actual = menuServiceV2.display(menu.getId());
        assertThat(actual.isDisplayed()).isTrue();
    }

    @DisplayName("????????? ?????? ??? ??????.")
    @Test
    void hide() {
        final Menu menu = MENU1();
        menuRepository.save(menu);
        final Menu actual = menuServiceV2.hide(menu.getId());
        assertThat(actual.isDisplayed()).isFalse();
    }

    @DisplayName("????????? ????????? ????????? ??? ??????.")
    @Test
    void findAll() {
        menuRepository.save(MENU1());
        menuRepository.save(NOT_DISPLAYED_MENU());

        final List<Menu> actual = menuServiceV2.findAll();
        assertThat(actual).hasSize(2);
    }

}
