package bgroup.stocktradingsystem.stsserver.controller;

import bgroup.stocktradingsystem.stsserver.domain.Stock;
import bgroup.stocktradingsystem.stsserver.domain.response.CustomResponse;
import bgroup.stocktradingsystem.stsserver.domain.response.Result;
import bgroup.stocktradingsystem.stsserver.service.StockService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class StockController {
    private final StockService stockService;

    private Gson gson = new Gson();

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * 获取所有股票信息
     *
     * @return 所有股票信息或失败原因
     */
    @RequestMapping(value = "/stock/all", method = GET)
    @ResponseBody
    public String fetchAllStock() {
        try {
            return new CustomResponse(new Result(true),
                    stockService.fetchAllStock()).toString();
        } catch(DataAccessException e) {
            SQLException exception = (SQLException)e.getCause();
            System.out.println(exception.toString());
            return new CustomResponse(new Result(false, "数据库异常: " + exception.toString())).toString();
        }
    }

    /**
     * 指定股票代码获取单只股票信息
     *
     * @param data 指定股票代码
     * @return 指定股票代码的股票或失败原因
     */
    @RequestMapping(value = "/stock/one", method = POST)
    @ResponseBody
    public String fetchOneStock(@RequestBody String data) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        String code = gson.fromJson(data, String.class);
        try {
            return new CustomResponse(new Result(true),
                    stockService.fetchCertainStock("stock_code = '" + code + "'").get(0)).toString();
        } catch(DataAccessException e) {
            SQLException exception = (SQLException)e.getCause();
            System.out.println(exception.toString());
            return new CustomResponse(new Result(false, "数据库异常: " + exception.toString())).toString();
        }
    }

    /**
     * 指定权限返回权限以下的股票
     *
     * @param priv 指定权限
     * @return 权限以下的一系列股票
     */
    @RequestMapping(value = "/stock/{priv}", method = GET)
    @ResponseBody
    public String fetchStockUnderPriv(@PathVariable String priv) {
        try {
            return new CustomResponse(new Result(true),
                    stockService.fetchCertainStock("stock_priv <= " + priv)).toString();
        } catch(DataAccessException e) {
            SQLException exception = (SQLException)e.getCause();
            System.out.println(exception.toString());
            return new CustomResponse(new Result(false, "数据库异常: " + exception.toString())).toString();
        }
    }

    @RequestMapping(value = "/stock/{priv}/like", method = POST)
    @ResponseBody
    public String fetchStockUnderPrivAndLike(@RequestBody String data, @PathVariable String priv) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        String like = gson.fromJson(data, String.class);
        like = processLike(like);
        try {
            return new CustomResponse(new Result(true),
                    stockService.fetchCertainStock("stock_priv <= " + priv + " AND " +
                            "(stock_code LIKE '" + like + "' OR " +
                            "stock_name LIKE '" + like + "')")).toString();
        } catch(DataAccessException e) {
            SQLException exception = (SQLException)e.getCause();
            System.out.println(exception.toString());
            return new CustomResponse(new Result(false, "数据库异常: " + exception.toString())).toString();
        }
    }

    private String processLike(String like) {
        StringBuilder result = new StringBuilder("%" + like.charAt(0));
        for(int i=1; i<like.length(); i++)
            result.append("%").append(like.charAt(i));
        return result.append("%").toString();
    }


    /**
     * @param data 新的股票信息
     * @return 成功或失败原因
     */
    @RequestMapping(value = "/stock/update", method = POST)
    @ResponseBody
    public String updateStock(@RequestBody String data) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        Stock stock = gson.fromJson(data, Stock.class);
        stockService.updateStock(stock);
        return new CustomResponse(new Result(true)).toString();
        // TODO 失败判断
    }

    /**
     * @param data 新的一系列股票信息
     * @return 成功或失败原因
     */
    @RequestMapping(value = "/stock/update_list", method = POST)
    @ResponseBody
    public String updateStockList(@RequestBody String data) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        Type listType = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> stocks = new Gson().fromJson(data, listType);
        stockService.updateStockList(stocks);
        return new CustomResponse(new Result(true)).toString();
        // TODO 失败判断
    }

    /**
     * @param data 待更新的股票列表
     * @param newState 新状态
     * @return 成功或失败原因
     */
    @RequestMapping(value = "/stock/update_list/state/{newState}", method = POST)
    @ResponseBody
    public String updateStockListState(@RequestBody String data, @PathVariable String newState) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        Type listType = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> stocks = new Gson().fromJson(data, listType);
        switch (newState) {
            case "stop":
                newState = "暂停交易";
                break;
            case "restore":
                newState = "正常交易";
                break;
            case "stop3":
                newState = "停牌三天";
                break;
            default:
                newState = "异常";
                break;
        }
        for (Stock stock : stocks)
            stock.setStockState(newState);
        stockService.updateStockList(stocks);
        return new CustomResponse(new Result(true)).toString();
        // TODO 失败判断
    }

    /**
     * @param data 待更新的股票列表
     * @param newLimit 新限制
     * @return 成功或失败原因
     */
    @RequestMapping(value = "/stock/update_list/limit/{newLimit}", method = POST)
    @ResponseBody
    public String updateStockListLimit(@RequestBody String data, @PathVariable String newLimit) {
        data = data.substring(1, data.length()-1).replace("\\", "");
        Type listType = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> stocks = new Gson().fromJson(data, listType);
        for (Stock stock : stocks) {
            if(newLimit.equals("-1"))
                stock.setStockLimit(-1.0);
            else
                stock.setStockLimit(Double.valueOf(newLimit) / 100.0);
        }
        stockService.updateStockList(stocks);
        return new CustomResponse(new Result(true)).toString();
        // TODO 失败判断
    }

}
