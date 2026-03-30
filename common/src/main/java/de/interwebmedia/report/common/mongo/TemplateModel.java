package de.interwebmedia.report.common.mongo;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity("report_templates")
public class TemplateModel {
    @Id
    private String id;
    private String name;
    private String description;
}
