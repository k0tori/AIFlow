package com.aiflow.mapper;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    @Select("SELECT id, document_id, chunk_index, content, " +
            "1 - (embedding <=> #{vector}::vector) AS similarity " +
            "FROM knowledge_chunk " +
            "ORDER BY embedding <=> #{vector}::vector " +
            "LIMIT #{topK}")
    List<KnowledgeChunk> similaritySearch(@Param("vector") String vector, @Param("topK") int topK);
}
