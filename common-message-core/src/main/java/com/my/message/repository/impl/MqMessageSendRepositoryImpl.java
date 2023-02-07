package com.my.message.repository.impl;

import com.my.message.entity.MqMessageSend;
import com.my.message.repository.MqMessageSendRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.my.message.MsgConstants.*;

/**
 * Class MqMessageSendRepositoryImpl
 *
 * @author linyang
 * @date 2020 /8/29
 */
@Repository
public class MqMessageSendRepositoryImpl implements MqMessageSendRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    /**
     * Instantiates a new Mq message send repository.
     *
     * @param jdbcTemplate the jdbc template
     */
    public MqMessageSendRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(TABLE_MQ_MESSAGE_SEND);
    }

    @Override
    public MqMessageSend findByMessageId(String messageId) {
        if (messageId == null) {
            return null;
        }

        try {
            return jdbcTemplate.queryForObject("SELECT * FROM mq_message_send WHERE message_id = ?",
                    new BeanPropertyRowMapper<>(MqMessageSend.class), messageId);
        } catch (EmptyResultDataAccessException emptyEx) {
            // 没有找到记录
            return null;
        }
    }

    @Override
    public List<MqMessageSend> findByStatus(Integer status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query("SELECT * FROM mq_message_send WHERE status = ?",
                new BeanPropertyRowMapper<>(MqMessageSend.class), status);
    }

    @Override
    public List<MqMessageSend> findByStatusAndCreatedDateBefore(Integer status, Date createdDate) {
        // FIXME: consider only return top 50 rows to avoid OOM and for performance reason
        return jdbcTemplate.query("SELECT * FROM mq_message_send WHERE status = ? AND created_date <= ? LIMIT 50",
                new BeanPropertyRowMapper<>(MqMessageSend.class), status, createdDate);
    }

    @Override
    public Number save(MqMessageSend message) {
        return jdbcInsert.execute(new BeanPropertySqlParameterSource(message));
    }

    @Override
    public int updateStatus(String messageId, Integer status, String originalContent) {
        if (messageId == null) {
            return 0;
        }
        //int optCounter = jdbcTemplate.queryForObject("SELECT opt_counter FROM mq_message_send WHERE message_id = ?", Integer.class, messageId);
        //return jdbcTemplate.update("UPDATE mq_message_send SET status = ?, origContent=? WHERE message_id = ? AND opt_counter = ?", status, originalContent, messageId, optCounter);
        // 从使用场景上来说，不会出现将状态从1更新为0的情况，多次将状态更新为1不是问题，所以暂不使用乐观锁
        return jdbcTemplate.update("UPDATE mq_message_send SET status = ?, origContent=?, updated_date=now(3) WHERE message_id = ?",
                status, originalContent, messageId);
    }

    @Override
    public int updateContent(String messageId, String content) {
        if (messageId == null) {
            return 0;
        }
        //int optCounter = jdbcTemplate.queryForObject("SELECT opt_counter FROM mq_message_send WHERE message_id = ?", Integer.class, messageId);
        //return jdbcTemplate.update("UPDATE mq_message_send SET status = ?, origContent=? WHERE message_id = ? AND opt_counter = ?", status, originalContent, messageId, optCounter);
        // 从使用场景上来说，不会出现将状态从1更新为0的情况，多次将状态更新为1不是问题，所以暂不使用乐观锁
        return jdbcTemplate.update("UPDATE mq_message_send SET content=?, updated_date=now(3) WHERE message_id = ?",
                content, messageId);
    }

    @Override
    public void deleteByUpdatedDateBefore(String date,Long start) {
        String sql = String.format("delete FROM %s where UPDATED_DATE < '%s' limit %s",TABLE_MQ_MESSAGE_SEND,date,start);
        jdbcTemplate.update(sql);
    }

    @Override
    public List<MqMessageSend> findByStatusAndUpdatedDateBefore(Integer status, Date createdDate,Long start) {
        // FIXME: consider only return top 200 rows to avoid OOM and for performance reason
        return jdbcTemplate.query("SELECT * FROM mq_message_send WHERE status = ? AND updated_date <= ? LIMIT ?,200",
                new BeanPropertyRowMapper<>(MqMessageSend.class), status, createdDate,start);
    }

    @Override
    public Long findCountByStatusAndUpdatedDateBefore(Integer status, Date updatedDate) {
        return jdbcTemplate.queryForObject("SELECT COUNT(0) FROM mq_message_send WHERE status = ? AND updated_date <= ?",
                Long.class, status, updatedDate);
    }


}
