DROP TABLE IF EXISTS record;
CREATE TABLE record (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  description varchar(255) DEFAULT NULL,
  content text,
  createdTime datetime DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=62549 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS test_student;
CREATE TABLE test_student (
  id bigint(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name varchar(20) DEFAULT NULL COMMENT '姓名',
  gender char(1) NOT NULL COMMENT '性别：男/女',
  age int(3) NOT NULL COMMENT '年龄',
  team_id bigint(10) DEFAULT NULL COMMENT '团队ID',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS test_team;
CREATE TABLE test_team (
  id bigint(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name varchar(20) DEFAULT NULL COMMENT '团队名',
  income decimal(6,2) DEFAULT NULL COMMENT '团队收入',
  rank int(3) DEFAULT NULL COMMENT '团队排名',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
