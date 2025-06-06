package com.br.marketing.service.mock;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;

/**
 * @ClassName MockService
 * @Author kongbx
 * @Date 2025/6/6 16:01
 */
public interface MockService {

    PageResultReturn getMockPolicyList(MockQueryDTO dto, MarketingUserDetail userDetail);

}
