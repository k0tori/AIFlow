package com.aiflow.mapper;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    @Select("SELECT id, document_id, chunk_index, content, " +
            "1 - (embedding <=> #{vector}::vector) AS similarity " +
            "FROM knowledge_chunk " +
            "ORDER BY embedding <=> #{vector}::vector " +
            "LIMIT #{topK}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "document_id", property = "documentId"),
            @Result(column = "chunk_index", property = "chunkIndex"),
            @Result(column = "content", property = "content"),
            @Result(column = "similarity", property = "similarity")
    })
    List<KnowledgeChunk> similaritySearch(@Param("vector") String vector, @Param("topK") int topK);
}
