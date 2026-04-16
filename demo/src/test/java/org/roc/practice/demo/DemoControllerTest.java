package org.roc.practice.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.roc.practice.DemoBeginner;
import org.roc.practice.demo.model.order.OrderRequest;
import org.roc.practice.demo.model.user.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoBeginner.class)
@AutoConfigureMockMvc
@DisplayName("DemoController 集成测试")
class DemoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ──────────────────────────────────────────────────────────────
    // 核验点 1: GET /demo/hello  →  统一返回 Result 包装
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("1. GET /demo/hello → bizCode=00000, data='Hello, roc-base!'")
    void hello_returnsSuccessResult() throws Exception {
        mockMvc.perform(get("/demo/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("00000"))
                .andExpect(jsonPath("$.data").value("Hello, roc-base!"));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 2: POST /demo/user/create  →  Create 分组校验
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("2a. POST /demo/user/create (合法) → bizCode=00000")
    void createUser_valid_returnsSuccess() throws Exception {
        UserRequest req = new UserRequest(null, "张三", 25, "13800138000", "1234567890");
        mockMvc.perform(post("/demo/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("00000"));
    }

    @Test
    @DisplayName("2b. POST /demo/user/create (userId 非 null) → PARAM_ERROR")
    void createUser_userIdNotNull_returnsParamError() throws Exception {
        UserRequest req = new UserRequest(1L, "张三", 25, "13800138000", "1234567890");
        mockMvc.perform(post("/demo/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bizCode").value("A0001"));
    }

    @Test
    @DisplayName("2c. POST /demo/user/create (userName 为空) → PARAM_ERROR")
    void createUser_blankUserName_returnsParamError() throws Exception {
        UserRequest req = new UserRequest(null, "", 25, "13800138000", "1234567890");
        mockMvc.perform(post("/demo/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bizCode").value("A0001"))
                .andExpect(jsonPath("$.msg").value(containsString("未填写用户姓名")));
    }

    @Test
    @DisplayName("2d. POST /demo/user/create (手机号格式错误) → PARAM_ERROR")
    void createUser_invalidPhone_returnsParamError() throws Exception {
        UserRequest req = new UserRequest(null, "张三", 25, "12345678901", "1234567890");
        mockMvc.perform(post("/demo/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0001"));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 3: PUT /demo/user/update  →  Update 分组校验
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("3a. PUT /demo/user/update (合法) → bizCode=00000")
    void updateUser_valid_returnsSuccess() throws Exception {
        UserRequest req = new UserRequest(1L, "李四", 30, "13900139000", "0987654321");
        mockMvc.perform(put("/demo/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("00000"));
    }

    @Test
    @DisplayName("3b. PUT /demo/user/update (userId 为 null) → PARAM_ERROR")
    void updateUser_userIdNull_returnsParamError() throws Exception {
        UserRequest req = new UserRequest(null, "李四", 30, "13900139000", "0987654321");
        mockMvc.perform(put("/demo/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0001"))
                .andExpect(jsonPath("$.msg").value(containsString("用户ID不能为空")));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 4: POST /demo/order/create  →  嵌套 @Valid 级联校验
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("4a. POST /demo/order/create (合法) → bizCode=00000")
    void createOrder_valid_returnsSuccess() throws Exception {
        UserRequest user = new UserRequest(null, "王五", 20, "13700137000", "abcdefghij");
        OrderRequest order = new OrderRequest(user, null, 199L, LocalDate.now().plusDays(1));
        mockMvc.perform(post("/demo/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("00000"));
    }

    @Test
    @DisplayName("4b. POST /demo/order/create (嵌套 userName 为空) → PARAM_ERROR")
    void createOrder_nestedUserNameBlank_returnsParamError() throws Exception {
        UserRequest user = new UserRequest(null, "", 20, "13700137000", "abcdefghij");
        OrderRequest order = new OrderRequest(user, null, 199L, LocalDate.now().plusDays(1));
        mockMvc.perform(post("/demo/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0001"));
    }

    @Test
    @DisplayName("4c. POST /demo/order/create (price 为负数) → PARAM_ERROR")
    void createOrder_negativePrice_returnsParamError() throws Exception {
        UserRequest user = new UserRequest(null, "王五", 20, "13700137000", "abcdefghij");
        OrderRequest order = new OrderRequest(user, null, -1L, LocalDate.now().plusDays(1));
        mockMvc.perform(post("/demo/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0001"))
                .andExpect(jsonPath("$.msg").value(containsString("支付金额必须是正数")));
    }

    @Test
    @DisplayName("4d. POST /demo/order/create (ts 为过去日期) → PARAM_ERROR")
    void createOrder_pastDate_returnsParamError() throws Exception {
        UserRequest user = new UserRequest(null, "王五", 20, "13700137000", "abcdefghij");
        OrderRequest order = new OrderRequest(user, null, 100L, LocalDate.now().minusDays(1));
        mockMvc.perform(post("/demo/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0001"))
                .andExpect(jsonPath("$.msg").value(containsString("你只能订阅今天以后的")));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 5: GET /demo/page  →  偏移分页 PageVo
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("5a. GET /demo/page?current=1&size=10 → 10 条记录，total=100")
    void page_firstPage_returnsCorrectPageVo() throws Exception {
        mockMvc.perform(get("/demo/page").param("current", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("00000"))
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.records", hasSize(10)))
                .andExpect(jsonPath("$.data.records[0].userId").value(1))
                .andExpect(jsonPath("$.data.records[9].userId").value(10));
    }

    @Test
    @DisplayName("5b. GET /demo/page?current=2&size=5 → 第 2 页，userId 从 6 开始")
    void page_secondPage_returnsCorrectRecords() throws Exception {
        mockMvc.perform(get("/demo/page").param("current", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records", hasSize(5)))
                .andExpect(jsonPath("$.data.records[0].userId").value(6))
                .andExpect(jsonPath("$.data.records[4].userId").value(10));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 6: GET /demo/scroll  →  游标分页 ScrollVo
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("6a. GET /demo/scroll?cursor=0&size=5 → 5 条，hasNext=true，nextCursor=5")
    void scroll_firstPage_returnsScrollVo() throws Exception {
        mockMvc.perform(get("/demo/scroll").param("cursor", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("00000"))
                .andExpect(jsonPath("$.data.records", hasSize(5)))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(5));
    }

    @Test
    @DisplayName("6b. GET /demo/scroll?cursor=5&size=5 → userId 从 6 开始")
    void scroll_secondPage_startsFromNextCursor() throws Exception {
        mockMvc.perform(get("/demo/scroll").param("cursor", "5").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].userId").value(6))
                .andExpect(jsonPath("$.data.nextCursor").value(10));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 7: GET /demo/exception/business  →  BusinessException
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("7. GET /demo/exception/business → bizCode=A0004, msg 含自定义文本")
    void businessException_returnsNotFoundCode() throws Exception {
        mockMvc.perform(get("/demo/exception/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0004"))
                .andExpect(jsonPath("$.msg").value(containsString("演示：目标资源不存在")));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 8: GET /demo/exception/system  →  未捕获异常兜底
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("8. GET /demo/exception/system → bizCode=C0001 (SYSTEM_ERROR)")
    void systemException_returnsSystemErrorCode() throws Exception {
        mockMvc.perform(get("/demo/exception/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("C0001"));
    }

    // ──────────────────────────────────────────────────────────────
    // 核验点 9: GET /demo/no-permission  →  Result.fail 直接构造错误返回
    // ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("9. GET /demo/no-permission → bizCode=A0003 (NO_PERMISSION)")
    void noPermission_returnsNoPermissionCode() throws Exception {
        mockMvc.perform(get("/demo/no-permission"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizCode").value("A0003"))
                .andExpect(jsonPath("$.msg").value("暂无操作权限"));
    }
}
