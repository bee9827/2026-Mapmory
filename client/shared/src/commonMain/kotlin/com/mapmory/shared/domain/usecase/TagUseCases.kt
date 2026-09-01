package com.mapmory.shared.domain.usecase

import com.mapmory.shared.domain.model.Tag
import com.mapmory.shared.domain.repository.TagRepository

class GetTagsUseCase(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(): Result<List<Tag>> = repository.getTags()
}

class CreateTagUseCase(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(name: String): Result<Tag> = repository.createTag(name)
}
