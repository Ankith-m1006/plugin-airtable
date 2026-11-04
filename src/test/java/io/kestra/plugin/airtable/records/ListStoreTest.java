package io.kestra.plugin.airtable.records;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.serializers.FileSerde;
import io.kestra.plugin.airtable.AirtableRecord;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class ListStoreTest {

    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @SuppressWarnings("unchecked")
    void storeWritesIonThatIonReadersCanReadBack() throws Exception {
        RunContext runContext = runContextFactory.of();
        java.util.List<AirtableRecord> records = java.util.List.of(
            new AirtableRecord("rec1", "2024-01-01T00:00:00.000Z", Map.of("Name", "Alice, Bob", "Note", "a=b {c}")),
            new AirtableRecord("rec2", "2024-01-02T00:00:00.000Z", Map.of("Name", "Zoë", "Count", 3))
        );

        URI uri = List.storeRecords(runContext, records);

        assertThat(uri.toString(), containsString("airtable_records.ion"));
        try (var reader = new BufferedReader(new InputStreamReader(runContext.storage().getFile(uri), StandardCharsets.UTF_8))) {
            java.util.List<Object> rows = FileSerde.readAll(reader).collectList().block();

            assertThat(rows, hasSize(2));
            Map<String, Object> first = (Map<String, Object>) rows.get(0);
            assertThat(first.get("id"), is("rec1"));
            Map<String, Object> fields = (Map<String, Object>) first.get("fields");
            assertThat(fields.get("Name"), is("Alice, Bob"));
            assertThat(fields.get("Note"), is("a=b {c}"));
            Map<String, Object> second = (Map<String, Object>) rows.get(1);
            assertThat(((Map<String, Object>) second.get("fields")).get("Name"), is("Zoë"));
        }
    }
}
