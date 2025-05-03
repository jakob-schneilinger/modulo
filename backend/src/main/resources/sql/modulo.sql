CREATE TABLE `components` (
	`id` INT NOT NULL AUTO_INCREMENT UNIQUE,
	`type` ENUM("text", "image", "board") NOT NULL,
	`owner_id` INT NOT NULL COMMENT 'foreign key',
	`width` INT NOT NULL,
	PRIMARY KEY(`id`)
);


CREATE TABLE `text_content` (
	`id` INT NOT NULL UNIQUE COMMENT 'foreign key',
	`text` VARCHAR(255) NOT NULL
);


CREATE TABLE `image_content` (
	`id` INT NOT NULL UNIQUE,
	`image_type` ENUM("png", "gif", "jpg") NOT NULL,
	`image` VARCHAR(255) NOT NULL
);


CREATE TABLE `board_content` (
	`id` INT NOT NULL UNIQUE,
	`board_name` VARCHAR(255) NOT NULL
);


CREATE TABLE `container_children` (
	`id_child` INT NOT NULL,
	`id_container` INT NOT NULL
);


CREATE TABLE `users` (
	`id` INT NOT NULL AUTO_INCREMENT UNIQUE,
	`username` VARCHAR(31),
	`display_name` VARCHAR(255),
	`email` VARCHAR(255) NOT NULL UNIQUE,
	`description` VARCHAR(4095),
	`avatar` VARCHAR(255),
	`avatar_type` ENUM("jpg", "png", "gif"),
	PRIMARY KEY(`id`)
);


CREATE TABLE `groups` (
	`id` INT NOT NULL AUTO_INCREMENT UNIQUE,
	`owner_id` INT NOT NULL,
	`name` VARCHAR(255),
	`is_friendlist` BOOLEAN NOT NULL,
	PRIMARY KEY(`id`)
);


CREATE TABLE `group_members` (
	`member_id` INT NOT NULL,
	`group_id` INT NOT NULL
);


CREATE TABLE `permissions` (
	`component_id` INT NOT NULL,
	`group_id` INT NOT NULL,
	`read` BOOLEAN,
	`write` BOOLEAN
);


CREATE TABLE `passwords` (
	`user_id` INT NOT NULL UNIQUE,
	`hash` VARCHAR(255) NOT NULL
);


CREATE TABLE `salts` (
	`user_id` INT NOT NULL UNIQUE,
	`salt` VARCHAR(255) NOT NULL
);


ALTER TABLE `text_content`
ADD FOREIGN KEY(`id`) REFERENCES `components`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `image_content`
ADD FOREIGN KEY(`id`) REFERENCES `components`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `board_content`
ADD FOREIGN KEY(`id`) REFERENCES `components`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `container_children`
ADD FOREIGN KEY(`id_child`) REFERENCES `components`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `container_children`
ADD FOREIGN KEY(`id_container`) REFERENCES `components`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `components`
ADD FOREIGN KEY(`owner_id`) REFERENCES `users`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `group_members`
ADD FOREIGN KEY(`group_id`) REFERENCES `groups`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `groups`
ADD FOREIGN KEY(`owner_id`) REFERENCES `users`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `group_members`
ADD FOREIGN KEY(`member_id`) REFERENCES `users`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `permissions`
ADD FOREIGN KEY(`component_id`) REFERENCES `components`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `permissions`
ADD FOREIGN KEY(`group_id`) REFERENCES `groups`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `passwords`
ADD FOREIGN KEY(`user_id`) REFERENCES `users`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE `salts`
ADD FOREIGN KEY(`user_id`) REFERENCES `users`(`id`)
ON UPDATE NO ACTION ON DELETE CASCADE;