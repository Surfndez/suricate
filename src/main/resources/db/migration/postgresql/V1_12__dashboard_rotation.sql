-- Creation of the slide table
CREATE TABLE PROJECT_SLIDE (
     id                  bigserial      NOT NULL,
     max_column          integer,
     widget_height       integer,
     css_style           text,
     created_by          VARCHAR(255)   NOT NULL,
     created_date        TIMESTAMP      NOT NULL,
     last_modified_by    VARCHAR(255),
     last_modified_date  TIMESTAMP,
     screenshot_id       bigint,
     project_id          bigint,
     CONSTRAINT PK_PROJECT_SLIDE_ID PRIMARY KEY (id),
     CONSTRAINT FK_PROJECT_SLIDE_SCREENSHOT_ID FOREIGN KEY (screenshot_id) REFERENCES asset(id),
     CONSTRAINT FK_PROJECT_SLIDE_PROJECT_ID FOREIGN KEY (project_id) REFERENCES project(id)
);

-- Split Project table into 2 Table (for now it's a 1 for 1 relation)
INSERT INTO PROJECT_SLIDE (id, max_column, widget_height, css_style, screenshot_id, project_id, created_by, created_date, last_modified_by, last_modified_date)
     SELECT p.id, p.max_column, p.widget_height, p.css_style, p.screenshot_id, p.id, p.created_by,p.created_date, p.last_modified_by, p.last_modified_date
     FROM project p;

-- Delete unused column from PROJECT table
ALTER TABLE PROJECT DROP COLUMN max_column;
ALTER TABLE PROJECT DROP COLUMN widget_height;
ALTER TABLE PROJECT DROP COLUMN css_style;
ALTER TABLE PROJECT DROP COLUMN screenshot_id;

-- Move PROJECT_WIDGET Foreign key from Project to Project slide
ALTER TABLE PROJECT_WIDGET DROP CONSTRAINT fk_project_widget_project_id;
ALTER TABLE PROJECT_WIDGET RENAME COLUMN project_id to project_slide_id;
ALTER TABLE PROJECT_WIDGET ADD CONSTRAINT FK_PROJECT_WIDGET_PROJECT_SLIDE_ID FOREIGN KEY (project_slide_id) REFERENCES PROJECT_SLIDE(id);