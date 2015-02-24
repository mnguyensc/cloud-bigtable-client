package com.google.cloud.bigtable.hbase.adapters;

import com.google.bigtable.v1.Cell;
import com.google.bigtable.v1.Column;
import com.google.bigtable.v1.Family;
import com.google.bigtable.v1.Row;
import com.google.cloud.bigtable.hbase.BigtableConstants;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapt a bigtable.v1.Row to an hbase client Result.
 * TODO(angusdavis): Rename to RowAdapter once anviltop has been fully removed.
 */
public class BigtableRowAdapter implements ResponseAdapter<Row, Result> {
  @Override
  public Result adaptResponse(Row response) {
    List<org.apache.hadoop.hbase.Cell> hbaseCells = new ArrayList<>();
    byte[] rowKey = response.getKey().toByteArray();

    for (Family family : response.getFamiliesList()) {
      byte[] familyNameBytes = Bytes.toBytes(family.getName());

      for (Column column : family.getColumnsList()) {
        byte[] columnQualifier = column.getQualifier().toByteArray();

        for (Cell cell : column.getCellsList()) {
          long hbaseTimestamp =
              BigtableConstants.HBASE_TIMEUNIT.convert(
                  cell.getTimestampMicros(), BigtableConstants.BIGTABLE_TIMEUNIT);
          KeyValue keyValue = new KeyValue(
              rowKey,
              familyNameBytes,
              columnQualifier,
              hbaseTimestamp,
              cell.getValue().toByteArray());

          hbaseCells.add(keyValue);
        }
      }
    }

    Collections.sort(hbaseCells, KeyValue.COMPARATOR);

    return Result.create(hbaseCells);
  }
}