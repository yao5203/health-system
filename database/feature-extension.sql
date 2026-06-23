USE health_system;

CREATE TABLE IF NOT EXISTS health_article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    summary VARCHAR(500),
    content TEXT,
    tags VARCHAR(255),
    cover_image VARCHAR(255),
    status INT DEFAULT 1 COMMENT '1发布 0草稿',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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
);

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
);

CREATE TABLE IF NOT EXISTS rule_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_key VARCHAR(100) UNIQUE NOT NULL,
    rule_name VARCHAR(120),
    rule_value VARCHAR(100),
    description VARCHAR(255),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO rule_config (rule_key, rule_name, rule_value, description)
VALUES
('risk_normal_score', '正常风险阈值', '88', '综合评分高于等于该值时判定为正常'),
('risk_mild_subhealth_score', '轻度亚健康阈值', '76', '综合评分高于等于该值时判定为轻度亚健康'),
('risk_subhealth_score', '中度亚健康阈值', '64', '综合评分高于等于该值时判定为中度亚健康'),
('risk_medium_score', '偏高风险阈值', '50', '综合评分高于等于该值时判定为偏高风险')
ON DUPLICATE KEY UPDATE
rule_name = VALUES(rule_name),
rule_value = VALUES(rule_value),
description = VALUES(description);

INSERT INTO health_article (title, category, summary, content, tags, cover_image, status)
VALUES
(
  '湿热体质人群的日常饮食控制要点',
  '饮食指南',
  '从热量、油脂、辛辣控制和三餐搭配四个方面，帮助湿热体质用户稳定代谢状态。',
  '湿热体质在饮食上应减少油炸、辛辣、甜饮和高盐加工食品摄入。建议早餐以杂粮、鸡蛋、豆制品为主，午餐重视优质蛋白和蔬菜比例，晚餐控制油脂和总热量。绿豆、冬瓜、苦瓜、芹菜等清淡食材更适合作为长期调养基础。',
  '湿热体质,饮食指南,三餐搭配',
  '',
  1
),
(
  '睡眠质量差时，如何用作息管理修复身体节律',
  '睡眠管理',
  '通过固定上床时间、晚间降刺激和晨间光照管理，逐步恢复更稳定的睡眠节律。',
  '如果长期出现入睡困难、夜间易醒或早醒，建议首先固定起床时间，其次提前一小时降低屏幕刺激，避免咖啡因和重口饮食进入夜间时段。白天增加自然光照和轻量运动，也能帮助夜间睡眠更快建立节律。',
  '睡眠,作息管理,节律',
  '',
  1
),
(
  '亚健康状态下的慢病预防重点',
  '慢病预防',
  '亚健康阶段往往是慢病风险积累期，重点应放在血压、血糖、体重和压力管理四个方面。',
  '当身体已经进入亚健康状态时，血压、血糖、体重和心理压力是最值得重点关注的四项指标。建议以每周复盘方式记录健康数据，结合饮食清淡化、运动规律化和睡眠稳定化，优先阻断向慢病演变的趋势。',
  '亚健康,慢病预防,血压,血糖',
  '',
  1
),
(
  '压力过大时的自我放松训练',
  '心理放松',
  '呼吸训练、步行冥想和任务减载可以帮助用户降低紧绷感和情绪消耗。',
  '高压力状态下，不要只依赖“忍耐”。更有效的方式包括：每天 10 分钟腹式呼吸、午后 15 分钟步行放松、把大任务拆分成更短时段执行，并在夜间减少高刺激内容。连续执行一周，通常能明显改善情绪疲劳感。',
  '压力管理,心理放松,呼吸训练',
  '',
  1
)
ON DUPLICATE KEY UPDATE
summary = VALUES(summary),
content = VALUES(content),
tags = VALUES(tags),
status = VALUES(status);
