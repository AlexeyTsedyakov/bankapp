package com.example.bankapp.exchange.service;

import com.example.bankapp.exchange.exceptions.CurrencyConversionException;
import com.example.bankapp.exchange.exceptions.InvalidRateException;
import com.example.bankapp.exchange.exceptions.RateNotFoundException;
import com.example.bankapp.exchange.model.ConvertResult;
import com.example.bankapp.exchange.model.Currency;
import com.example.bankapp.exchange.model.ExchangeRate;
import com.example.bankapp.exchange.repository.RateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ExchangeService.class)
@ActiveProfiles("test")
public class ExchangeServiceTest {
    @Autowired
    private ExchangeService service;
    @MockitoBean
    private RateRepository repository;

    @Test
    void getRates_returnsRatesList() {
        List<ExchangeRate> rates = List.of(
                new ExchangeRate(Currency.USD, new BigDecimal("100.00"), new BigDecimal("105.00")),
                new ExchangeRate(Currency.USD, new BigDecimal("90.00"), new BigDecimal("95.00"))
        );
        when(repository.findAll()).thenReturn(rates);

        List<ExchangeRate> found = service.getRates();

        assertThat(found).isEqualTo(rates);
        verify(repository, times(1)).findAll();
    }

    @Test
    void getRate_existingCurrency_returnsRate() {
        var usdRate = new ExchangeRate(Currency.USD, new BigDecimal("90.00"), new BigDecimal("95.00"));
        when(repository.findByCurrency(Currency.USD)).thenReturn(Optional.of(usdRate));

        ExchangeRate found = service.getRate(Currency.USD);

        assertThat(found).isEqualTo(usdRate);
        verify(repository, times(1)).findByCurrency(Currency.USD);
    }

    @Test
    void getRate_nonExistingCurrency_throwsException() {
        when(repository.findByCurrency(Currency.USD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRate(Currency.USD))
                .isInstanceOf(RateNotFoundException.class);
        verify(repository, times(1)).findByCurrency(Currency.USD);
    }

    @Test
    void setRates_validRates_savesAllRates() {
        var usdRate = new ExchangeRate(Currency.USD, new BigDecimal("90.00"), new BigDecimal("95.00"));
        var eurRate = new ExchangeRate(Currency.EUR, new BigDecimal("100.00"), new BigDecimal("105.00"));

        service.setRates(List.of(usdRate, eurRate));

        verify(repository, times(1)).save(usdRate);
        verify(repository, times(1)).save(eurRate);
    }

    @Test
    void setRate_validRate_savesRate() {
        var usdRate = new ExchangeRate(Currency.USD, new BigDecimal("90.00"), new BigDecimal("95.00"));

        service.setRate(usdRate);

        verify(repository, times(1)).save(usdRate);
    }

    @Test
    void setRate_nullRate_throwsException() {
        assertThatThrownBy(() -> service.setRate(null))
                .isInstanceOf(InvalidRateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void setRate_nullSellRate_throwsException() {
        var rate = new ExchangeRate(Currency.USD, new BigDecimal("90.00"), null);

        assertThatThrownBy(() -> service.setRate(rate))
                .isInstanceOf(InvalidRateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void setRate_zeroOrNegativeSellRate_throwsException() {
        var zeroSellRate = new ExchangeRate(Currency.USD, new BigDecimal("90.00"), BigDecimal.ZERO);
        var negativeSellRate = new ExchangeRate(Currency.USD, new BigDecimal("90.00"), new BigDecimal("-10.00"));

        assertThatThrownBy(() -> service.setRate(zeroSellRate))
                .isInstanceOf(InvalidRateException.class);

        assertThatThrownBy(() -> service.setRate(negativeSellRate))
                .isInstanceOf(InvalidRateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void setRate_nullBuyRate_throwsException() {
        var rate = new ExchangeRate(Currency.USD, null, new BigDecimal("95.00"));

        assertThatThrownBy(() -> service.setRate(rate))
                .isInstanceOf(InvalidRateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void setRate_zeroOrNegativeBuyRate_throwsException() {
        var zeroBuyRate = new ExchangeRate(Currency.USD, BigDecimal.ZERO, new BigDecimal("95.00"));
        var negativeBuyRate = new ExchangeRate(Currency.USD, new BigDecimal("-10.00"), new BigDecimal("95.00"));

        assertThatThrownBy(() -> service.setRate(zeroBuyRate))
                .isInstanceOf(InvalidRateException.class);

        assertThatThrownBy(() -> service.setRate(negativeBuyRate))
                .isInstanceOf(InvalidRateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void setRate_rubCurrency_throwsException() {
        var rubRate = new ExchangeRate(Currency.RUB, new BigDecimal("1.00"), new BigDecimal("1.00"));

        assertThatThrownBy(() -> service.setRate(rubRate))
                .isInstanceOf(InvalidRateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void convert_nullFromCurrency_throwsException() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThatThrownBy(() -> service.convert(null, Currency.USD, amount))
                .isInstanceOf(CurrencyConversionException.class);
    }

    @Test
    void convert_nullToCurrency_throwsException() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThatThrownBy(() -> service.convert(Currency.USD, null, amount))
                .isInstanceOf(CurrencyConversionException.class);
    }

    @Test
    void convert_sameCurrencies_throwsException() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThatThrownBy(() -> service.convert(Currency.USD, Currency.USD, amount))
                .isInstanceOf(CurrencyConversionException.class);
    }

    @Test
    void convert_nullAmount_throwsException() {
        assertThatThrownBy(() -> service.convert(Currency.USD, Currency.RUB, null))
                .isInstanceOf(CurrencyConversionException.class);
    }

    @Test
    void convert_zeroOrNegativeAmount_throwsException() {
        assertThatThrownBy(() -> service.convert(Currency.USD, Currency.RUB, BigDecimal.ZERO))
                .isInstanceOf(CurrencyConversionException.class);

        assertThatThrownBy(() -> service.convert(Currency.USD, Currency.RUB, new BigDecimal("-10.00")))
                .isInstanceOf(CurrencyConversionException.class);
    }

    @Test
    void convert_fromBaseCurrencyRub_returnsConvertedAmount() {
        var to = new ExchangeRate(Currency.USD, new BigDecimal("91.20"), new BigDecimal("92.05"));
        BigDecimal amount = new BigDecimal("920.50");
        when(repository.findByCurrency(Currency.USD)).thenReturn(Optional.of(to));

        ConvertResult result = service.convert(Currency.RUB, Currency.USD, amount);

        assertThat(result).isNotNull();
        assertThat(result.converted()).isEqualByComparingTo("10.00"); // 920.50 / 92.05 = 10.00
        verify(repository).findByCurrency(Currency.USD);
        verify(repository, never()).findByCurrency(Currency.RUB);
    }

    @Test
    void convert_toBaseCurrencyRub_returnsConvertedAmount() {
        var from = new ExchangeRate(Currency.USD, new BigDecimal("91.20"), new BigDecimal("92.05"));
        BigDecimal amount = new BigDecimal("10.00");
        when(repository.findByCurrency(Currency.USD)).thenReturn(Optional.of(from));

        ConvertResult result = service.convert(Currency.USD, Currency.RUB, amount);

        assertThat(result).isNotNull();
        assertThat(result.converted()).isEqualByComparingTo("912.00"); // 10.00 * 91.20 = 912.00
        verify(repository).findByCurrency(Currency.USD);
        verify(repository, never()).findByCurrency(Currency.RUB);
    }

    @Test
    void convert_sourceCurrencyIsNotBase_returnsConvertedAmount() {
        var from = new ExchangeRate(Currency.CNY, new BigDecimal("12.55"), new BigDecimal("12.78"));
        var to = new ExchangeRate(Currency.USD, new BigDecimal("91.20"), new BigDecimal("92.05"));
        BigDecimal amount = new BigDecimal("100.00");
        when(repository.findByCurrency(from.getCurrency()))
                .thenReturn(Optional.of(from));
        when(repository.findByCurrency(to.getCurrency()))
                .thenReturn(Optional.of(to));

        ConvertResult result = service.convert(from.getCurrency(), to.getCurrency(), amount);

        assertThat(result).isNotNull();
        assertThat(result.converted()).isEqualByComparingTo("13.63"); // 100 * 12.55 / 92.05
        verify(repository).findByCurrency(Currency.CNY);
        verify(repository).findByCurrency(Currency.USD);
    }

    @Test
    void convert_sourceRateNotFound_throwsException() {
        BigDecimal amount = new BigDecimal("100.00");
        when(repository.findByCurrency(Currency.USD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert(Currency.USD, Currency.EUR, amount))
                .isInstanceOf(RateNotFoundException.class);
    }

    @Test
    void convert_targetRateNotFound_throwsException() {
        var from = new ExchangeRate(Currency.USD, new BigDecimal("91.20"), new BigDecimal("92.05"));
        BigDecimal amount = new BigDecimal("100.00");
        when(repository.findByCurrency(Currency.USD)).thenReturn(Optional.of(from));
        when(repository.findByCurrency(Currency.EUR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert(Currency.USD, Currency.EUR, amount))
                .isInstanceOf(RateNotFoundException.class);
    }
}
