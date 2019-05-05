package org.gnuhpc.bigdata.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Data
public class TopicPartitionReplicaAssignment implements Comparable<TopicPartitionReplicaAssignment>{
  String topic;
  int partition;
  List<Integer> replicas;
  List<String> log_dirs;

  @Override
  public int compareTo(TopicPartitionReplicaAssignment that) {
    if (this.topic.equals(that.topic)) {
      return (this.partition - that.partition);
    } else {
      return this.topic.compareTo(that.topic);
    }
  }
}
