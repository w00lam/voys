package com.voys.notes.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeneratedNoteGeneratorTests {

	private final GeneratedNoteGenerator generator = new GeneratedNoteGenerator();

	@Test
	void generateBuildsDeterministicDraftFromTranscriptText() {
		var draft = generator.generate("""
			The team reviewed launch strategy and risk owners.
			Key risk is unclear rollout sequencing.
			Follow up with Alex on mitigation owners.
			""");

		assertThat(draft.summary()).isEqualTo("The team reviewed launch strategy and risk owners.");
		assertThat(draft.keyPoints()).containsExactly(
			"The team reviewed launch strategy and risk owners.",
			"Key risk is unclear rollout sequencing.",
			"Follow up with Alex on mitigation owners."
		);
		assertThat(draft.actionItems()).containsExactly("Follow up with Alex on mitigation owners.");
	}
}
