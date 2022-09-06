/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.db.instance.configs.migrations;

import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.primaryKey;
import static org.jooq.impl.DSL.unique;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V0_40_3_003__CreateSyncStats extends BaseJavaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(V0_40_3_003__CreateSyncStats.class);

  @Override
  public void migrate(final Context context) throws Exception {
    LOGGER.info("Running migration: {}", this.getClass().getSimpleName());

    // Warning: please do not use any jOOQ generated code to write a migration.
    // As database schema changes, the generated jOOQ code can be deprecated. So
    // old migration may not compile if there is any generated code.
    final DSLContext ctx = DSL.using(context.getConnection());
    createSyncStatsTable(ctx);
  }

  private static void createSyncStatsTable(final DSLContext ctx) {
    final Field<UUID> id = DSL.field("id", SQLDataType.UUID.nullable(false));
    final Field<Integer> attemptId = DSL.field("attempt_id", SQLDataType.INTEGER.nullable(false));
    final Field<Integer> recordsEmitted = DSL.field("records_emitted", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> bytesEmitted = DSL.field("bytes_emitted", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> sourceStateMessagesEmitted = DSL.field("source_state_messages_emitted", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> destinationStateMessagesEmitted = DSL.field("destination_state_messages_emitted", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> recordsCommitted = DSL.field("records_committed", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> meanSecondsBeforeSourceStateMessageEmitted =
        DSL.field("mean_seconds_before_source_state_message_emitted", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> maxSecondsBeforeSourceStateMessageEmitted =
        DSL.field("max_seconds_before_source_state_message_emitted", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> meanSecondsBetweenStateMessageEmittedandCommitted =
        DSL.field("mean_seconds_between_state_message_emitted_and_committed", SQLDataType.INTEGER.nullable(true));
    final Field<Integer> maxSecondsBetweenStateMessageEmittedandCommitted =
        DSL.field("max_seconds_between_state_message_emitted_and_committed", SQLDataType.INTEGER.nullable(true));
    final Field<OffsetDateTime> createdAt =
        DSL.field("created_at", SQLDataType.TIMESTAMPWITHTIMEZONE.nullable(false).defaultValue(currentOffsetDateTime()));
    final Field<OffsetDateTime> updatedAt =
        DSL.field("updated_at", SQLDataType.TIMESTAMPWITHTIMEZONE.nullable(false).defaultValue(currentOffsetDateTime()));

    ctx.createTableIfNotExists("sync_stats")
        .columns(id, attemptId, recordsEmitted, bytesEmitted, sourceStateMessagesEmitted, destinationStateMessagesEmitted, recordsCommitted,
            meanSecondsBeforeSourceStateMessageEmitted, maxSecondsBeforeSourceStateMessageEmitted, meanSecondsBetweenStateMessageEmittedandCommitted,
            maxSecondsBetweenStateMessageEmittedandCommitted, createdAt, updatedAt)
        .constraints(primaryKey(id), unique(attemptId))
        .execute();

    ctx.createIndex("attempt_id_idx").on("sync_stats", "attempt_id").execute();
  }

}
