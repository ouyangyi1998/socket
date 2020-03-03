package pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Request {
    private String sn;
    private String timestamp;
    private String trans_code;
    private String req_data;
    private Integer seq;
    private String sign;
}
