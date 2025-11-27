/*
 * @ (#) EventLogRepositoryImpl.java    1.0    26/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.repositories.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 26/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.request.EventLogFilterRequest;
import fit.monitoring_service.entities.EventLog;
import fit.monitoring_service.repositories.EventLogRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<EventLog> searchEventLogs(EventLogFilterRequest filter, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // 1. Lọc theo Keyword (tìm trong Action hoặc Message)
        if (StringUtils.hasText(filter.getKeyword())) {
            String regex = ".*" + filter.getKeyword() + ".*";
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("action").regex(regex, "i"),
                    Criteria.where("message").regex(regex, "i")
            );
            criteriaList.add(keywordCriteria);
        }

        // 2. Lọc theo Action cụ thể
        if (StringUtils.hasText(filter.getAction())) {
            criteriaList.add(Criteria.where("action").is(filter.getAction()));
        }

        // 3. Lọc theo Operator [cite: 451]
        if (StringUtils.hasText(filter.getOperator())) {
            criteriaList.add(Criteria.where("operator").regex(".*" + filter.getOperator() + ".*", "i"));
        }

        // 4. Lọc theo Source Service (IAM, WAREHOUSE...)
        if (StringUtils.hasText(filter.getSourceService())) {
            criteriaList.add(Criteria.where("source_service").is(filter.getSourceService()));
        }

        // 5. Lọc theo khoảng thời gian (Date Range)
        if (filter.getFromDate() != null) {
            criteriaList.add(Criteria.where("created_at").gte(filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            criteriaList.add(Criteria.where("created_at").lte(filter.getToDate()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Đếm tổng số record trước khi phân trang
        long total = mongoTemplate.count(query, EventLog.class);

        // Áp dụng phân trang và sắp xếp
        query.with(pageable);

        List<EventLog> eventLogs = mongoTemplate.find(query, EventLog.class);

        return new PageImpl<>(eventLogs, pageable, total);
    }
}
