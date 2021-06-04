package com.appsdeveloper.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.appsdeveloper.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SpeakerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Speaker.class);
        Speaker speaker1 = new Speaker();
        speaker1.setId(1L);
        Speaker speaker2 = new Speaker();
        speaker2.setId(speaker1.getId());
        assertThat(speaker1).isEqualTo(speaker2);
        speaker2.setId(2L);
        assertThat(speaker1).isNotEqualTo(speaker2);
        speaker1.setId(null);
        assertThat(speaker1).isNotEqualTo(speaker2);
    }
}
