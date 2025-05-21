drop table if exists user_info;
CREATE TABLE `user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` varchar(64) NOT NULL COMMENT '平台用户id',
  `user_name` varchar(64) NOT NULL DEFAULT '微信用户' COMMENT '用户名',
  `user_type` varchar(64) NOT NULL DEFAULT '1' COMMENT '用户类型',
  `phone` varchar(64) NULL COMMENT '手机号',
  `open_id` varchar(128) NULL COMMENT '微信用户唯一标识',
  `union_id` varchar(128) NULL COMMENT '微信全平台用户唯一标识',
  `avatar_url` varchar(256) NULL DEFAULT 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0' COMMENT '用户头像',
  `create_time` datetime NULL COMMENT '创建时间',
  `update_time` datetime NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `user_info_index_user_id`(`user_id`) USING BTREE,
  INDEX `user_info_index_open_id`(`open_id`) USING BTREE,
  INDEX `user_info_index_phone`(`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 comment = '用户信息表';