package com.my.message.repository.impl;

import com.my.message.entity.MqMessageReceive;
import com.my.message.repository.MqMessageReceiveRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static com.my.message.MsgConstants.*;

/**
 * Class MqMessageReceiveRepositoryImpl
 *
 * @author linyang
 * @date 2020 /8/29
 */
@Repository
public class MqMessageReceiveRepositoryImpl implements MqMessageReceiveRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Instantiates a new Mq message receive repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public MqMessageReceiveRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(TABLE_MQ_MESSAGE_RECEIVE);
    }

    @Override
    public MqMessageReceive findByMessageId(String messageId) {
        if (messageId == null) {
            return null;
        }

        try {
            return jdbcTemplate.queryForObject("SELECT * FROM mq_message_receive WHERE message_id = ?",
                    MqMessageReceive.class, messageId);
        } catch (EmptyResultDataAccessException emptyEx) {
            // 没有找到记录
            return null;
        }
    }

    @Override
    public Number save(MqMessageReceive message) {
        return jdbcInsert.execute(new BeanPropertySqlParameterSource(message));
    }

    @Override
    public int updateOriginalContent(String messageId, String originalContent) {
        if (messageId == null) {
            return 0;
        }
        return jdbcTemplate.update("UPDATE mq_message_receive SET origContent = ? WHERE message_id = ?",
                originalContent, messageId);
    }

    @Override
    public void deleteByUpdatedDateBefore(String date,Long start) {
        String sql = String.format("delete FROM %s where UPDATED_DATE < '%s' limit %s",TABLE_MQ_MESSAGE_RECEIVE,date,start);
        jdbcTemplate.update(sql);
    }

    @Override
    public Long findCountByUpdatedDateBefore(Date updatedDate) {
        return jdbcTemplate.queryForObject("SELECT COUNT(0) FROM mq_message_receive WHERE updated_date <= ?",
                Long.class, updatedDate);
    }


    @Override
    public List<MqMessageReceive> findByUpdatedDateBefore(Date createdDate,Long start) {
        // FIXME: consider only return top 200 rows to avoid OOM and for performance reason
        return jdbcTemplate.query("SELECT * FROM mq_message_receive WHERE updated_date <= ? LIMIT ?,200",
                new BeanPropertyRowMapper<>(MqMessageReceive.class), createdDate,start);
    }

    @Override
    public List<MqMessageReceive> findByCreatedDateBefore(Date createdDate) {
        // FIXME: consider only return top 50 rows to avoid OOM and for performance reason
        return jdbcTemplate.query("SELECT * FROM mq_message_receive WHERE created_date <= ? LIMIT 50",
                new BeanPropertyRowMapper<>(MqMessageReceive.class), createdDate);
    }
}
