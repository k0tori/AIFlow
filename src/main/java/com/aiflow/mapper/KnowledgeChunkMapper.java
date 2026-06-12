package com.aiflow.mapper;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    @Select("WITH ranked AS (" +
            "  SELECT id, document_id, chunk_index, content, " +
            "         1 - (embedding <=> #{vector}::vector) AS similarity," +
            "         ROW_NUMBER() OVER (ORDER BY embedding <=> #{vector}::vector) as rn" +
            "  FROM knowledge_chunk" +
            ") " +
            "SELECT id, document_id, chunk_index, content, similarity " +
            "FROM ranked WHERE rn <= #{topK}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "document_id", property = "documentId"),
            @Result(column = "chunk_index", property = "chunkIndex"),
            @Result(column = "content", property = "content"),
            @Result(column = "similarity", property = "similarity")
    })
    List<KnowledgeChunk> similaritySearch(@Param("vector") String vector, @Param("topK") int topK);

    /**
     * Batch insert chunks (defined in KnowledgeChunkMapper.xml).
     */
    int insertBatch(@Param("list") List<KnowledgeChunk> chunks);
}
