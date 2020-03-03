package pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response {
    private String sn;
    private String timestamp;
    private String sign;
    private String rsp_code;
    private String rsp_msg;
    private String trans_code;
    private String rsp_data;
    private Integer seq;
}
