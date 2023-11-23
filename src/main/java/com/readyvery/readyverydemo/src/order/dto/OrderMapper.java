package com.readyvery.readyverydemo.src.order.dto;

import static com.readyvery.readyverydemo.global.Constant.*;
import static org.hibernate.type.descriptor.java.JdbcTimeJavaType.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.readyvery.readyverydemo.domain.Cart;
import com.readyvery.readyverydemo.domain.CartItem;
import com.readyvery.readyverydemo.domain.CartOption;
import com.readyvery.readyverydemo.domain.Foodie;
import com.readyvery.readyverydemo.domain.FoodieOption;
import com.readyvery.readyverydemo.domain.FoodieOptionCategory;
import com.readyvery.readyverydemo.domain.ImgSize;
import com.readyvery.readyverydemo.domain.Order;
import com.readyvery.readyverydemo.domain.Receipt;
import com.readyvery.readyverydemo.src.order.config.TossPaymentConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {
	private final TossPaymentConfig tossPaymentConfig;

	public FoodyDetailRes foodieToFoodyDetailRes(Foodie foodie, Long inout) {
		Long price = determinePrice(foodie, inout);

		return FoodyDetailRes.builder()
			.name(foodie.getName())
			.imgUrl(foodie.getImgUrl() != null ? IMG_URL + foodie.getFoodieCategory().getStore().getEngName() + "/"
				+ foodie.getImgUrl() : null)
			.price(price)
			.category(
				foodie.getFoodieOptionCategory()
					.stream()
					.map(this::foodieOptionCategoryToOptionCategoryDto)
					.toList())
			.build();
	}

	public Long determinePrice(Foodie foodie, Long inout) {
		if (Objects.equals(inout, EAT_IN)) {
			return foodie.getPrice();
		} else if (Objects.equals(inout, TAKE_OUT)) {
			return foodie.getTakeOut() != null ? foodie.getTakeOut().getPrice() : foodie.getPrice();
		}
		// inout이 EAT_IN도 TAKE_OUT도 아닌 경우, 기본 값 반환
		return foodie.getPrice();
	}

	private OptionCategoryDto foodieOptionCategoryToOptionCategoryDto(FoodieOptionCategory category) {
		return OptionCategoryDto.builder()
			.name(category.getName())
			.essential(category.isRequired())
			.options(
				category.getFoodieOptions()
					.stream()
					.map(this::foodyOptionToOptionDto)
					.toList())
			.build();
	}

	private FoodyOptionDto foodyOptionToOptionDto(FoodieOption option) {
		return FoodyOptionDto.builder()
			.idx(option.getId())
			.name(option.getName())
			.price(option.getPrice())
			.build();
	}

	public CartAddRes cartToCartAddRes(CartItem cartItem) {
		return CartAddRes.builder()
			.cartItemId(cartItem.getId())
			.build();
	}

	public CartEidtRes cartToCartEditRes(CartItem cartItem) {
		return CartEidtRes.builder()
			.idx(cartItem.getId())
			.count(cartItem.getCount())
			.build();
	}

	public CartItemDeleteRes cartToCartItemDeleteRes(CartItem cartItem) {
		return CartItemDeleteRes.builder()
			.idx(cartItem.getId())
			.build();
	}

	public CartResetRes cartToCartResetRes(Cart cart) {
		return CartResetRes.builder()
			.idx(cart.getId())
			.build();
	}

	public CartGetRes cartToCartGetRes(Cart cart, Long inout) {
		return CartGetRes.builder()
			.name(cart.getStore().getName())
			.imgUrl(cart.getStore()
				.getImgs()
				.stream()
				.filter(storeImg -> storeImg.getImgSize() == ImgSize.CAFE_LOGO)
				.map(storeImg -> IMG_URL + storeImg.getStore().getEngName() + "/" + storeImg.getImgUrl())
				.findFirst()
				.orElse(null))
			.carts(
				cart.getCartItems()
					.stream()
					.map(cartItem -> cartItemToCartDto(cartItem, inout))
					.toList())
			.totalPrice(
				cart.getCartItems()
					.stream()
					.mapToLong(cartItem -> cartItemTotalPrice(cartItem, inout))
					.sum())
			.build();
	}

	private CartDto cartItemToCartDto(CartItem cartItem, Long inout) {
		return CartDto.builder()
			.idx(cartItem.getId())
			.name(cartItem.getFoodie().getName())
			.count(cartItem.getCount())
			// img
			.imgUrl(cartItem.getFoodie().getImgUrl() != null
				? IMG_URL + cartItem.getFoodie().getFoodieCategory().getStore().getEngName() + "/"
				+ cartItem.getFoodie().getImgUrl()
				: null)
			.totalPrice(cartItemTotalPrice(cartItem, inout))
			.options(
				cartItem.getCartOptions()
					.stream()
					.map(this::cartOptionToOptionDto)
					.toList())
			.build();
	}

	private OptionDto cartOptionToOptionDto(CartOption cartOption) {
		return OptionDto.builder()
			.idx(cartOption.getId())
			.name(cartOption.getFoodieOption().getName())
			.price(cartOption.getFoodieOption().getPrice())
			.build();
	}

	private Long cartItemTotalPrice(CartItem cartItem, Long inout) {
		Long optionsPriceSum = cartItem.getCartOptions()
			.stream()
			.mapToLong(cartOption -> cartOption.getFoodieOption().getPrice())
			.sum();

		Long totalPrice = optionsPriceSum + determinePrice(cartItem.getFoodie(), inout);
		return totalPrice * cartItem.getCount();
	}

	public TosspaymentMakeRes orderToTosspaymentMakeRes(Order order) {
		return TosspaymentMakeRes.builder()
			.orderId(order.getOrderId())
			.orderName(order.getOrderName())
			.successUrl(tossPaymentConfig.getTossSuccessUrl())
			.failUrl(tossPaymentConfig.getTossFailUrl())
			.customerEmail("test@naver.com")
			.customerName("test")
			.amount(order.getAmount())
			.build();
	}

	public Receipt tosspaymentDtoToReceipt(TosspaymentDto tosspaymentDto, Order order) {
		return Receipt.builder()
			.order(order)
			.type(tosspaymentDto.getType())
			.mid(tosspaymentDto.getMid())
			.currency(tosspaymentDto.getCurrency())
			.balanceAmount(tosspaymentDto.getBalanceAmount())
			.suppliedAmount(tosspaymentDto.getSuppliedAmount())
			.status(tosspaymentDto.getStatus())
			.requestedAt(tosspaymentDto.getRequestedAt())
			.approvedAt(tosspaymentDto.getApprovedAt())
			.lastTransactionKey(tosspaymentDto.getLastTransactionKey())
			.vat(tosspaymentDto.getVat())
			.taxFreeAmount(tosspaymentDto.getTaxFreeAmount())
			.taxExemptionAmount(tosspaymentDto.getTaxExemptionAmount())
			.cancels(tosspaymentDto.getCancels() != null ? tosspaymentDto.getCancels().toString() : null)
			.card(tosspaymentDto.getCard() != null ? tosspaymentDto.getCard().toString() : null)
			.receipt(tosspaymentDto.getReceipt() != null ? tosspaymentDto.getReceipt().toString() : null)
			.checkout(tosspaymentDto.getCheckout() != null ? tosspaymentDto.getCheckout().toString() : null)
			.easyPay(tosspaymentDto.getEasyPay() != null ? tosspaymentDto.getEasyPay().toString() : null)
			.country(tosspaymentDto.getCountry())
			.failure(tosspaymentDto.getFailure() != null ? tosspaymentDto.getFailure().toString() : null)
			.discount(tosspaymentDto.getDiscount() != null ? tosspaymentDto.getDiscount().toString() : null)
			.virtualAccount(
				tosspaymentDto.getVirtualAccount() != null ? tosspaymentDto.getVirtualAccount().toString() : null)
			.transfer(tosspaymentDto.getTransfer() != null ? tosspaymentDto.getTransfer().toString() : null)
			.cashReceipt(tosspaymentDto.getCashReceipt() != null ? tosspaymentDto.getCashReceipt().toString() : null)
			.cashReceipts(tosspaymentDto.getCashReceipts() != null ? tosspaymentDto.getCashReceipts().toString() : null)
			.build();
	}

	public FailDto makeFailDto(String code, String message) {
		return FailDto.builder()
			.code(code)
			.message(message)
			.build();
	}

	public HistoryRes ordersToHistoryRes(List<Order> orders) {
		return HistoryRes.builder()
			.receipts(
				orders
					.stream()
					.map(this::orderToReceiptHistoryDto)
					.toList())
			.build();
	}

	private ReceiptHistoryDto orderToReceiptHistoryDto(Order order) {
		return ReceiptHistoryDto.builder()
			.dateTime(order.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
			.name(order.getStore().getName())
			.imgUrl(order.getStore()
				.getImgs()
				.stream()
				.filter(storeImg -> storeImg.getImgSize() == ImgSize.CAFE_LOGO)
				.map(storeImg -> IMG_URL + storeImg.getStore().getEngName() + "/" + storeImg.getImgUrl())
				.findFirst()
				.orElse(null))
			.orderName(order.getOrderName())
			.amount(order.getAmount())
			.orderId(order.getOrderId())
			.build();
	}

	public CurrentRes orderToCurrentRes(Order order) {
		return CurrentRes.builder()
			.name(order.getStore().getName())
			.orderNum(order.getId())
			.progress(order.getProgress())
			.orderName(order.getOrderName())
			.estimatedTime(order.getEstimatedTime() != null
				? order.getEstimatedTime().format(DateTimeFormatter.ofPattern(TIME_FORMAT)) : null)
			.build();
	}

	public DefaultRes tosspaymentDtoToCancelRes() {
		return DefaultRes.builder()
			.message("취소 성공")
			.build();
	}
}
