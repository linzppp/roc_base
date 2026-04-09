package org.roc.practice.core;

import lombok.Data;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
