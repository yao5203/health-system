CREATE DATABASE IF NOT EXISTS health_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE health_system;

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80),
  password VARCHAR(120),
  age INT,
  gender VARCHAR(20),
  phone VARCHAR(30),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS doctor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80),
  password VARCHAR(120),
  specialty VARCHAR(120),
  expertise_tags VARCHAR(255),
  introduction TEXT,
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS system_admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80),
  password VARCHAR(120),
  display_name VARCHAR(80),
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  blood_pressure_high INT,
  blood_pressure_low INT,
  blood_pressure_context VARCHAR(50),
  height DOUBLE,
  weight DOUBLE,
  blood_sugar DOUBLE,
  blood_sugar_context VARCHAR(50),
  heart_rate INT,
  heart_rate_context VARCHAR(50),
  blood_oxygen DOUBLE,
  blood_oxygen_context VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  score DOUBLE,
  health_level VARCHAR(50),
  constitution_type VARCHAR(80),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content VARCHAR(500),
  type INT,
  category VARCHAR(80),
  dimension VARCHAR(80),
  applicable_constitution VARCHAR(80),
  applicable_health_level VARCHAR(80),
  sort_order INT DEFAULT 0,
  is_active INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS question_option (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT,
  content VARCHAR(120),
  score INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  question_id BIGINT,
  option_id BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS questionnaire_quality (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  duration_seconds INT,
  total_questions INT,
  answered_questions INT,
  missing_count INT,
  straight_line_rate DOUBLE,
  quality_score INT,
  consistency_level VARCHAR(50),
  warnings TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(160),
  constitution_type VARCHAR(80),
  health_level VARCHAR(50),
  diet TEXT,
  drink TEXT,
  sport TEXT,
  lifestyle TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_plan_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_type VARCHAR(50),
  name VARCHAR(120),
  constitution_type VARCHAR(80),
  risk_level VARCHAR(80),
  dimension VARCHAR(80),
  min_score INT,
  max_score INT,
  content TEXT,
  priority INT DEFAULT 0,
  is_active INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  category VARCHAR(50),
  summary VARCHAR(500),
  content TEXT,
  tags VARCHAR(255),
  cover_image VARCHAR(255),
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plan_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  plan_id BIGINT,
  plan_title VARCHAR(120),
  constitution_type VARCHAR(50),
  health_level VARCHAR(20),
  diet TEXT,
  drink TEXT,
  sport TEXT,
  lifestyle TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_checkin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  checkin_date DATE,
  plan_title VARCHAR(120),
  sleep_hours DOUBLE,
  exercise_minutes INT,
  stress_level INT,
  mood_score INT,
  weight DOUBLE,
  blood_pressure_high INT,
  blood_pressure_low INT,
  remark TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  content TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS consultation_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  doctor_id BIGINT,
  admin_id BIGINT,
  issue_type VARCHAR(80),
  title VARCHAR(160),
  detail TEXT,
  preferred_tag VARCHAR(120),
  status VARCHAR(50),
  admin_note TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  assign_time DATETIME,
  close_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS consultation_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  consultation_id BIGINT,
  sender_type VARCHAR(50),
  sender_id BIGINT,
  sender_name VARCHAR(80),
  content TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS change_proposal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  proposer_doctor_id BIGINT,
  proposer_name VARCHAR(80),
  target_type VARCHAR(80),
  action_type VARCHAR(80),
  target_id BIGINT,
  title VARCHAR(160),
  summary TEXT,
  payload_json LONGTEXT,
  status VARCHAR(50),
  reviewer_admin_id BIGINT,
  reviewer_name VARCHAR(80),
  review_comment TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  review_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rule_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_key VARCHAR(100) UNIQUE NOT NULL,
  rule_name VARCHAR(120),
  rule_value VARCHAR(100),
  description VARCHAR(255),
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS system_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operation VARCHAR(255),
  operator VARCHAR(80),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO system_admin (id, username, password, display_name, status)
VALUES (1, 'admin', '123456', '系统管理员', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username), password = VALUES(password), display_name = VALUES(display_name), status = VALUES(status);

INSERT INTO doctor (id, username, password, specialty, expertise_tags, introduction, status)
VALUES
(1, 'doctor', '123456', '全科健康管理', '睡眠管理,饮食指导,运动建议', '负责用户咨询处理、健康快照查看和康养内容提案。', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username), password = VALUES(password), specialty = VALUES(specialty), expertise_tags = VALUES(expertise_tags), introduction = VALUES(introduction), status = VALUES(status);

INSERT INTO `user` (id, username, password, age, gender, phone)
VALUES
(1, 'testuser', '123456', 22, '男', '13800000000')
ON DUPLICATE KEY UPDATE username = VALUES(username), password = VALUES(password), age = VALUES(age), gender = VALUES(gender), phone = VALUES(phone);

INSERT INTO rule_config (rule_key, rule_name, rule_value, description)
VALUES
('risk_normal_score', '正常风险阈值', '88', '综合评分高于等于该值时判定为正常'),
('risk_mild_subhealth_score', '轻度亚健康阈值', '76', '综合评分高于等于该值时判定为轻度亚健康'),
('risk_subhealth_score', '中度亚健康阈值', '64', '综合评分高于等于该值时判定为中度亚健康'),
('risk_medium_score', '偏高风险阈值', '50', '综合评分高于等于该值时判定为偏高风险'),
('risk_high_score', '高风险阈值', '0', '综合评分低于偏高风险阈值时判定为高风险'),
('level_excellent_score', '优秀健康等级阈值', '88', '综合评分高于等于该值时健康等级为优秀'),
('level_good_score', '良好健康等级阈值', '76', '综合评分高于等于该值时健康等级为良好'),
('level_general_score', '一般健康等级阈值', '62', '综合评分高于等于该值时健康等级为一般'),
('level_risk_score', '风险健康等级阈值', '0', '综合评分低于一般阈值时健康等级为风险')
ON DUPLICATE KEY UPDATE rule_name = VALUES(rule_name), rule_value = VALUES(rule_value), description = VALUES(description);

INSERT INTO health_plan (id, title, constitution_type, health_level, diet, drink, sport, lifestyle)
VALUES
(1, '平衡稳态型康养方案', '平和体质', '良好', '保持三餐规律，主食、蛋白质和蔬菜比例均衡。', '每日少量多次饮水，避免长期以含糖饮料替代白水。', '每周保持三到五次中等强度运动。', '保持稳定作息，避免连续熬夜。'),
(2, '气虚体质康养方案', '气虚体质', '一般', '适当增加优质蛋白和温和易消化食物。', '饮水以温水为主，避免大量冰饮。', '以散步、八段锦等低强度运动开始。', '注意劳逸结合，避免长时间过度疲劳。'),
(3, '湿热体质康养方案', '湿热体质', '一般', '减少油炸、辛辣、高糖食物摄入。', '适量饮水，避免甜饮和酒精刺激。', '选择快走、慢跑等有氧运动帮助代谢。', '保持清淡饮食和规律作息。'),
(4, '阴虚体质康养方案', '阴虚体质', '一般', '饮食以清淡滋润为主，减少辛辣燥热食物。', '保证饮水，不长期处于口干状态。', '以舒缓运动为主，避免过度消耗。', '重视睡眠恢复和情绪平稳。')
ON DUPLICATE KEY UPDATE title = VALUES(title), diet = VALUES(diet), drink = VALUES(drink), sport = VALUES(sport), lifestyle = VALUES(lifestyle);

INSERT INTO health_article (id, title, category, summary, content, tags, status)
VALUES
(1, '湿热体质人群的日常饮食控制要点', '饮食指南', '从油脂、糖分和作息角度说明湿热体质的日常管理。', '湿热体质在饮食上应减少油炸、辛辣、甜饮和高盐加工食品摄入，优先选择清淡食材，并保持规律运动。', '湿热体质,饮食,康养', 1),
(2, '睡眠质量较差时如何调整作息', '睡眠管理', '通过固定入睡时间、减少夜间刺激和改善睡眠环境来恢复节律。', '如果长期存在入睡困难或夜间易醒，可先固定起床时间，睡前减少屏幕刺激，并保持卧室安静和舒适。', '睡眠,作息,恢复', 1),
(3, '亚健康状态下的慢病预防重点', '慢病预防', '关注血压、血糖、体重和压力管理。', '亚健康阶段应重视健康指标的连续记录，结合饮食清淡化、运动规律化和压力管理，降低长期健康风险。', '亚健康,慢病预防,健康管理', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), summary = VALUES(summary), content = VALUES(content), tags = VALUES(tags), status = VALUES(status);

INSERT INTO question (id, content, type, category, dimension, applicable_constitution, applicable_health_level, sort_order, is_active)
VALUES
(1, '您是否经常感到精力充沛、身体状态稳定？', 1, '平和体质', '身体健康状况', '通用', '通用', 1, 1),
(2, '您是否容易感到疲劳、气短或懒得说话？', 2, '气虚体质', '身体健康状况', '气虚体质', '通用', 2, 1),
(3, '您是否经常觉得身体困重、面部油腻？', 2, '湿热体质', '身体健康状况', '湿热体质', '通用', 3, 1),
(4, '您是否经常心烦、手足心热、口干咽燥？', 2, '阴虚体质', '身体健康状况', '阴虚体质', '通用', 4, 1),
(5, '您近期胃口和消化情况是否比较稳定？', 1, '平和体质', '身体健康状况', '通用', '通用', 5, 1),
(6, '您是否经常出现头晕、乏力或恢复较慢？', 2, '气虚体质', '身体健康状况', '气虚体质', '通用', 6, 1),
(7, '您是否容易出现口苦、口黏或大便黏滞？', 2, '湿热体质', '身体健康状况', '湿热体质', '通用', 7, 1),
(8, '您是否容易出现盗汗或夜间燥热？', 2, '阴虚体质', '身体健康状况', '阴虚体质', '通用', 8, 1),
(9, '您是否能保持规律三餐？', 1, '平和体质', '生活习惯评估', '通用', '通用', 9, 1),
(10, '您是否经常熬夜或作息不固定？', 2, '阴虚体质', '生活习惯评估', '通用', '通用', 10, 1),
(11, '您是否每周有三次以上运动？', 1, '平和体质', '生活习惯评估', '通用', '通用', 11, 1),
(12, '您是否经常吃油炸、辛辣或高糖食物？', 2, '湿热体质', '生活习惯评估', '湿热体质', '通用', 12, 1),
(13, '您是否久坐时间较长且活动较少？', 2, '气虚体质', '生活习惯评估', '通用', '通用', 13, 1),
(14, '您是否能主动控制饮水和饮食量？', 1, '平和体质', '生活习惯评估', '通用', '通用', 14, 1),
(15, '您是否经常在睡前长时间使用电子设备？', 2, '阴虚体质', '生活习惯评估', '通用', '通用', 15, 1),
(16, '您是否会定期记录体重、血压或睡眠情况？', 1, '平和体质', '生活习惯评估', '通用', '通用', 16, 1),
(17, '您近期是否容易紧张、焦虑或烦躁？', 2, '阴虚体质', '心理健康状况', '通用', '通用', 17, 1),
(18, '您遇到压力时是否能够较快调整情绪？', 1, '平和体质', '心理健康状况', '通用', '通用', 18, 1),
(19, '您是否经常因为小事感到心烦？', 2, '阴虚体质', '心理健康状况', '阴虚体质', '通用', 19, 1),
(20, '您是否容易因为疲劳而情绪低落？', 2, '气虚体质', '心理健康状况', '气虚体质', '通用', 20, 1),
(21, '您是否有适合自己的放松方式？', 1, '平和体质', '心理健康状况', '通用', '通用', 21, 1),
(22, '您是否经常感觉压力影响食欲或睡眠？', 2, '阴虚体质', '心理健康状况', '通用', '通用', 22, 1),
(23, '您是否能保持较稳定的心情？', 1, '平和体质', '心理健康状况', '通用', '通用', 23, 1),
(24, '您是否容易因为工作学习节奏变化而不适？', 2, '气虚体质', '心理健康状况', '通用', '通用', 24, 1),
(25, '您通常能保证七小时左右睡眠吗？', 1, '平和体质', '睡眠质量评估', '通用', '通用', 25, 1),
(26, '您是否经常入睡困难？', 2, '阴虚体质', '睡眠质量评估', '阴虚体质', '通用', 26, 1),
(27, '您是否夜间容易醒来或睡眠较浅？', 2, '阴虚体质', '睡眠质量评估', '阴虚体质', '通用', 27, 1),
(28, '您醒来后是否感觉身体恢复较好？', 1, '平和体质', '睡眠质量评估', '通用', '通用', 28, 1),
(29, '您是否因为压力导致睡眠变差？', 2, '阴虚体质', '睡眠质量评估', '通用', '通用', 29, 1),
(30, '您是否能保持比较固定的入睡和起床时间？', 1, '平和体质', '睡眠质量评估', '通用', '通用', 30, 1)
ON DUPLICATE KEY UPDATE content = VALUES(content), type = VALUES(type), category = VALUES(category), dimension = VALUES(dimension), applicable_constitution = VALUES(applicable_constitution), applicable_health_level = VALUES(applicable_health_level), sort_order = VALUES(sort_order), is_active = VALUES(is_active);

DELETE FROM question_option WHERE question_id BETWEEN 1 AND 30;

INSERT INTO question_option (question_id, content, score)
SELECT q.id, option_text, option_score
FROM question q
JOIN (
  SELECT '总是' AS option_text, 1 AS option_score UNION ALL
  SELECT '经常', 2 UNION ALL
  SELECT '有时', 3 UNION ALL
  SELECT '很少', 4 UNION ALL
  SELECT '从不', 5
) opts ON 1 = 1
WHERE q.id BETWEEN 1 AND 30 AND q.type = 2;

INSERT INTO question_option (question_id, content, score)
SELECT q.id, option_text, option_score
FROM question q
JOIN (
  SELECT '总是' AS option_text, 5 AS option_score UNION ALL
  SELECT '经常', 4 UNION ALL
  SELECT '有时', 3 UNION ALL
  SELECT '很少', 2 UNION ALL
  SELECT '从不', 1
) opts ON 1 = 1
WHERE q.id BETWEEN 1 AND 30 AND q.type = 1;
