package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class BlockBulkRepository {

  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public void bulkInsert(final User blocker, final List<User> blockedUsers) {
    final String saveQuery = "INSERT INTO block(blocker_id, blocked_user_id) VALUES(?, ?)";
    jdbcTemplate.batchUpdate(saveQuery, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(final PreparedStatement ps, final int i) throws SQLException {
          ps.setLong(1, blocker.getId());
          ps.setLong(2, blockedUsers.get(i).getId());
      }
      @Override
      public int getBatchSize() {
        return blockedUsers.size();
      }
    });
  }
}
