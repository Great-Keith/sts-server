package bgroup.stocktradingsystem.stsserver.controller;

import bgroup.stocktradingsystem.stsserver.domain.AdminAccount;
import bgroup.stocktradingsystem.stsserver.domain.CorporateAccount;
import bgroup.stocktradingsystem.stsserver.domain.PersonalAccount;
import bgroup.stocktradingsystem.stsserver.domain.response.CustomResponse;
import bgroup.stocktradingsystem.stsserver.domain.response.Result;
import bgroup.stocktradingsystem.stsserver.service.SecuritiesAccountService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class SecuritiesController {
    @Autowired
    SecuritiesAccountService securitiesAccountService;

    private Gson gson = new Gson();

    @RequestMapping(value = "/securities/new/personal", method = POST)
    @ResponseBody
    public String createPersonalAccount(@RequestBody String data) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        PersonalAccount account = gson.fromJson(data, PersonalAccount.class);
        securitiesAccountService.createPersonalAccount(account);
        return new CustomResponse(new Result(true)).toString();
    }

    @RequestMapping(value = "/securities/new/corporate", method = POST)
    @ResponseBody
    public String createCorporateAccount(@RequestBody String data) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        CorporateAccount account = gson.fromJson(data, CorporateAccount.class);
        securitiesAccountService.createCorporateAccount(account);
        return new CustomResponse(new Result(true)).toString();
    }

}
