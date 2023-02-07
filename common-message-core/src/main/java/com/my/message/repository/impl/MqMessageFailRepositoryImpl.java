package com.my.message.repository.impl;

import com.my.message.entity.MqMessageFail;
import com.my.message.repository.MqMessageFailRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static com.my.message.MsgConstants.TABLE_MQ_MESSAGE_FAIL;

/**
 * Class MqMessageFailRepositoryImpl
 *
 * @author linyang
 * @date 2020 /8/29
 */
@Repository
public class MqMessageFailRepositoryImpl implements MqMessageFailRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Instantiates a new Mq message fail repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public MqMessageFailRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(TABLE_MQ_MESSAGE_FAIL);
    }

    @Override
    public Number save(MqMessageFail message) {
        return jdbcInsert.execute(new BeanPropertySqlParameterSource(message));
    }


    @Override
    public void deleteByUpdatedDateBefore(String date,Long start) {
        String sql = String.format("delete FROM %s where UPDATED_DATE < '%s' limit %s",TABLE_MQ_MESSAGE_FAIL,date,start);
        jdbcTemplate.update(sql);
    }

    @Override
    public Long findCountByUpdatedDateBefore(Date updatedDate) {
        return jdbcTemplate.queryForObject("SELECT COUNT(0) FROM mq_message_fail WHERE updated_date <= ?",
                Long.class, updatedDate);
    }


    @Override
    public List<MqMessageFail> findByUpdatedDateBefore( Date createdDate,Long start) {
        // FIXME: consider only return top 200 rows to avoid OOM and for performance reason
        return jdbcTemplate.query("SELECT * FROM mq_message_fail WHERE updated_date <= ? LIMIT ?,200",
                new BeanPropertyRowMapper<>(MqMessageFail.class), createdDate,start);
    }

    @Override
    public List<MqMessageFail> findByCreatedDateBefore(Date createdDate) {
        // FIXME: consider only return top 50 rows to avoid OOM and for performance reason
        return jdbcTemplate.query("SELECT * FROM mq_message_fail WHERE created_date <= ? LIMIT 50",
                new BeanPropertyRowMapper<>(MqMessageFail.class), createdDate);
    }
}
