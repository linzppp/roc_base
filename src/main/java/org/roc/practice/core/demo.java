package org.roc.practice.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
public class demo {
    @GetMapping("/info")
    public String getInfo(){
        return "hello";
    }

    @GetMapping("/infoForO")
    public DemoVO getInfoForO(){
        return new DemoVO("roc lin","成功", Money.ofMinor(CurrencyUnit.AUD, 865));
    }

    @GetMapping("/money")
    public Money getMoney(){
        return Money.ofMinor(CurrencyUnit.CHF, 995);
    }

    @PostMapping("/create")
    public DemoVO createM(@RequestBody @Valid DemoCreateReq req) {
        return new DemoVO(req.getUsername(), "成功", Money.ofMajor(CurrencyUnit.AUD,req.getAmount()));
    }

    @Data
    static class DemoVO {
        private String username;
        private String res;
        private Money m;

        public DemoVO(String username, String res, Money m){
            this.username=username;
            this.res=res;
            this.m=m;
        }
    }

    @Data
    static class DemoCreateReq {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 20, message = "用户名长度须在2到20之间")
        private String username;

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须为正数")
        private Long amount;
    }
}
