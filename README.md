# 校园物品共享接口文档

## 1.账户管理模块

### 1.1 登录并获取 Token 接口

**POST** `http://localhost:8080/as/accounts/login`

**说明**：用户通过账号密码或验证码进行身份验证。验证成功后，服务端将返回一个加密的 Token 字符串，后续所有受保护的接口均需在 Header 中携带此 Token。

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| type | Integer | 是 | 登录方式：1-密码登录；2-验证码登录 |
| password | String | 是 | 密码或验证码内容 |
| email | String | 否 | 绑定邮箱 |
| studentId | String | 否 | 校园学号 |
| phone | String | 否 | 手机号 |
| rememberMe | Boolean | 否 | 7天免密登录（默认：false） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "登录成功",  // 提示信息
  "ts": 1707154098000, 
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 身份验证 Token
}

```

### 1.2 管理端登录并获取 Token 接口

**POST** `http://localhost:8080/as/accounts/admin/login`

**说明**：管理员通过用户名和密码进行身份验证。验证成功后，返回管理端专用的加密 Token，用于访问后台管理系统相关资源。

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| username | String | 是 | 管理员用户名 |
| password | String | 是 | 登录密码 |
| rememberMe | Boolean | 否 | 7天免密登录（默认：false） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "管理端登录成功",
  "ts": 1707154240000, 
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 管理端身份验证 Token
}

```

### 1.3 退出登录接口

**POST** `http://localhost:8080/as/accounts/logout`

**说明**：用户主动登出当前系统。服务端接收请求后将使当前的 Token 失效，确保账号安全，后续请求将无法再使用该 Token 访问受限资源。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "退出成功",  // 提示信息
  "ts": 1707154560000, 
  "data": {}         // 成功时返回空对象
}

```

### 1.4 刷新 Token 接口

**GET** `http://localhost:8080/as/accounts/refresh`

**说明**：在 Access Token 过期后，客户端可通过此接口并携带 Cookie 中的 Refresh Token 获取新的 Access Token。支持普通用户（client）和管理员（admin）两种模式，通过 `X-Client-Type` 进行区分。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| X-Client-Type | Header | String | 是 | 客户端类型：`client` (用户), `admin` (管理员) |
| refresh | Cookie | String | 否 | 当 `X-Client-Type` 为 `client` 时必传 |
| admin-refresh | Cookie | String | 否 | 当 `X-Client-Type` 为 `admin` 时必传 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "刷新成功",  // 提示信息
  "ts": 1707154800000, 
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // 新生成的 Access Token
}

```

## 2.角色管理模块

### 2.1 查询角色列表接口

**GET** `http://localhost:8080/as/roles/list`

**说明**：查询系统中定义的全部权限角色列表。返回结果包含角色唯一标识、角色名称、详细描述以及记录的创建与更新时间。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707155100000, 
  "data": [          // 权限角色列表
    {
      "id": 1,                     // 主键ID
      "name": "admin",             // 角色名称
      "description": "超级管理员",   // 角色权限范围描述
      "createdAt": 1707155100000,  // 创建时间戳（毫秒）
      "updatedAt": 1707155100000   // 更新时间戳（毫秒）
    },
    {
      "id": 2,
      "name": "user",
      "description": "普通学生用户",
      "createdAt": 1707155100000,
      "updatedAt": 1707155100000
    }
  ]
}

```

### 2.2 根据 ID 查询角色接口

**GET** `http://localhost:8080/as/roles/{id}`

**说明**：通过角色唯一标识 ID 获取特定角色的详细信息，包括角色名称、权限描述以及相关的审计时间戳。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |
| id | Path | Integer | 是 | 角色唯一标识 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707155400000, 
  "data": {          // 角色详细信息
    "id": 1,                     // 主键ID
    "name": "admin",             // 角色名称
    "description": "超级管理员",   // 角色权限范围描述
    "createdAt": 1707155100000,  // 创建时间戳（毫秒）
    "updatedAt": 1707155100000   // 更新时间戳（毫秒）
  }
}

```

### 2.3 新增角色接口

**POST** `http://localhost:8080/as/roles`

**说明**：在系统中创建一个新的权限角色。需要提供角色名称（唯一标识）及相关描述，系统将自动生成主键 ID 并记录创建时间。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| name | String | 是 | 角色名称（如：guest、editor） |
| description | String | 否 | 角色权限范围或用途描述 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "新增成功",  // 提示信息
  "ts": 1707155700000, 
  "data": {          // 新增成功的角色信息
    "id": 10,                    // 系统分配的主键ID
    "name": "guest",             // 角色名称
    "description": "临时访问者",   // 角色描述
    "createdAt": 1707155700000,  // 创建时间戳（毫秒）
    "updatedAt": 1707155700000   // 更新时间戳（毫秒）
  }
}

```

### 2.4 修改角色信息接口

**PUT** `http://localhost:8080/as/roles/{id}`

**说明**：根据角色 ID 修改既有角色的名称或描述信息。常用于更新角色的权限定义或修正名称。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |
| id | Path | Integer | 是 | 角色唯一标识 ID |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| name | String | 否 | 新的角色名称 |
| description | String | 否 | 新的角色描述 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "修改成功",  // 提示信息
  "ts": 1707156000000, 
  "data": {}         // 成功时返回空对象
}

```

### 2.5 删除角色信息接口

**DELETE** `http://localhost:8080/as/roles/{id}`

**说明**：根据角色 ID 物理删除指定的角色记录。请在调用前确认该角色未关联任何活跃用户，否则可能会导致关联权限失效。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |
| id | Path | Integer | 是 | 角色唯一标识 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "删除成功",  // 提示信息
  "ts": 1707156300000, 
  "data": {}         // 成功时返回空对象
}

```

### 2.6 根据用户 ID 查询角色信息接口

**GET** `http://localhost:8080/as/roles/user/{userId}`

**说明**：通过用户唯一标识 ID 获取该用户当前所属的角色信息。该接口常用于前端根据用户角色动态渲染功能菜单或进行页面级的权限控制。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |
| userId | Path | Integer | 是 | 用户唯一标识 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707156600000, 
  "data": {          // 关联的角色实体信息
    "id": 2,                     // 角色主键ID
    "name": "user",              // 角色名称 (如: admin, user)
    "description": "普通学生用户"  // 角色描述
  }
}

```

## 3.chat模块
### 3.1 AI 聊天对话接口

**POST** `http://localhost:8080/ai/chat`

**说明**：与 AI 助手进行实时对话。该接口采用 **SSE (Server-Sent Events)** 流式传输技术，能够实时推送 AI 生成的内容。通过 `memoryId` 维持会话上下文，实现连续对话功能。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| memoryId | String | 是 | 会话记忆 ID（用于关联历史聊天上下文） |
| message | String | 是 | 用户输入的聊天消息内容 |

**Returns (text/event-stream)**

响应以数据流形式返回，每一帧为一个标准的 SSE 消息对象：

```json
{
  "id": "msg_123456",    // 事件 ID
  "event": "message",    // 事件类型
  "data": "你好",         // AI 推送的具体文本片段
  "retry": {             // 重连间隔（可选）
    "seconds": 3,
    "nanos": 0
  },
  "comment": ""          // 注释说明
}

```

> **注意**：由于是流式响应，客户端需要使用 `EventSource` 或支持流式读取的 Fetch API 进行处理。当 `data` 返回特定结束标识（如 `[DONE]`）时，表示本次回答结束。

## 4.物品分类模块
### 4.1 获取所有物品分类接口

**GET** `http://localhost:8080/is/categories`

**说明**：查询校园物品共享平台中所有定义的物品分类。返回列表包含分类的名称、图标、排序优先级及启用状态。该接口通常用于首页分类导航或发布物品时的类别选择。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707157200000, 
  "data": [          // 物品分类列表
    {
      "id": 1,                     // 分类 ID
      "name": "电子产品",           // 分类名称
      "description": "手机、电脑、充电宝等", // 分类描述
      "icon": "http://...",        // 分类图标 URL
      "sortOrder": 1,              // 排序序号（数值越小越靠前）
      "isActive": true,            // 是否启用
      "createdAt": 1707157200000,  // 创建时间戳
      "updatedAt": 1707157200000   // 更新时间戳
    },
    {
      "id": 2,
      "name": "书籍资料",
      "description": "课本、教材、课外书",
      "icon": "http://...",
      "sortOrder": 2,
      "isActive": true,
      "createdAt": 1707157200000,
      "updatedAt": 1707157200000
    }
  ]
}

```

### 4.2 管理员获取所有物品分类接口

**GET** `http://localhost:8080/is/categories/admin`

**说明**：管理端专用接口，用于获取系统内所有的物品分类列表。与普通用户接口相比，该接口通常用于后台管理页面的分类维护，返回包含禁用状态在内的全量分类数据。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录 token |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707157500000, 
  "data": [          // 物品分类列表 (全量)
    {
      "id": 1,                     // 分类 ID
      "name": "电子产品",           // 分类名称
      "description": "手机、电脑、充电宝等", 
      "icon": "http://...",        // 图标地址
      "sortOrder": 1,              // 排序权重
      "isActive": true,            // 启用状态
      "createdAt": 1707157200000, 
      "updatedAt": 1707157200000
    },
    {
      "id": 5,
      "name": "其他",
      "description": "不属于常用分类的物品",
      "icon": "http://...",
      "sortOrder": 99,
      "isActive": false,           // 已禁用的分类在管理端可见
      "createdAt": 1707157300000,
      "updatedAt": 1707157300000
    }
  ]
}

```

### 4.3 新增物品分类接口

**POST** `http://localhost:8080/is/categories`

**说明**：由管理员在系统中创建新的物品分类。通过此接口可以定义分类的名称、描述、图标以及显示顺序，并设置初始启用状态。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录 token |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| name | String | 是 | 分类名称（如：代步工具、运动器材），需唯一 |
| description | String | 否 | 分类描述，说明包含的物品类型 |
| icon | String | 否 | 分类图标 URL 或 标识符 |
| sortOrder | Integer | 否 | 排序权重，数值越小越靠前（默认 0） |
| isActive | Boolean | 否 | 是否立即启用（true/false） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "新增成功",  // 提示信息
  "ts": 1707157800000, 
  "data": {}         // 成功时返回空对象
}

```

### 4.4 删除物品分类接口

**DELETE** `http://localhost:8080/is/categories/{id}`

**说明**：根据分类 ID 物理删除指定的物品分类。执行此操作前，请确保该分类下已无关联物品，否则可能会因数据库外键约束导致删除失败。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录 token |
| id | Path | Integer | 是 | 物品分类唯一标识 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "删除成功",  // 提示信息
  "ts": 1707158100000, 
  "data": {}         // 成功时返回空对象
}

```

### 4.5 更新物品分类接口

**PUT** `http://localhost:8080/is/categories/{id}`

**说明**：根据分类 ID 修改现有物品分类的信息。管理员可以更新分类名称、描述、图标、排序权重以及是否启用状态。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录 token |
| id | Path | Integer | 是 | 物品分类唯一标识 ID |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| name | String | 否 | 分类名称（如电子产品、书籍） |
| description | String | 否 | 分类描述，说明包含的物品类型 |
| icon | String | 否 | 分类图标 URL/标识 |
| sortOrder | Integer | 否 | 排序顺序，数值越小越靠前 |
| isActive | Boolean | 否 | 是否启用 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "更新成功",  // 提示信息
  "ts": 1707158400000, 
  "data": {}         // 成功时返回空对象
}

```

## 5.用户管理模块
### 5.1 用户注册接口

**POST** `http://localhost:8080/us/users/register`

**说明**：新用户通过提供昵称、邮箱、密码及学籍信息进行账号注册。邮箱和学号在系统中具有唯一性，注册成功后可用于后续登录。

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| username | String | 是 | 用户昵称 |
| email | String | 是 | 用户邮箱（唯一，用于登录/找回密码） |
| password | String | 是 | 登录密码（前端应进行基础加密/哈希处理） |
| studentId | String | 是 | 学号（唯一，用于身份校验） |
| school | String | 是 | 所属学校 |
| department | String | 否 | 所属院系 |
| grade | String | 否 | 年级（如：2023级） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "注册成功",  // 提示信息
  "ts": 1707158700000, 
  "data": {}         // 成功时返回空对象
}

```

### 5.2 查询用户详情接口

**POST** `http://localhost:8080/us/users/detail/{isStaff}`

**说明**：根据用户登录凭证（邮箱/学号/手机号）查询该用户的核心身份信息。该接口通常用于系统内部鉴权、获取用户所属角色及会话策略。通过路径参数 `isStaff` 区分是否为内部工作人员查询。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| isStaff | Path | Boolean | 是 | 是否为工作人员（true-是，false-否） |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| type | Integer | 是 | 登录方式：1-密码登录；2-验证码登录 |
| password | String | 是 | 验证密码 |
| email | String | 否 | 账户邮箱 |
| studentId | String | 否 | 用户学号 |
| phone | String | 否 | 用户手机号 |
| rememberMe | Boolean | 否 | 是否启用7天免密登录 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707159000000, 
  "data": {          // 用户登录详情 DTO
    "userId": 2004151376778743810, // 用户唯一 ID
    "roleId": 2,                   // 角色 ID
    "rememberMe": true             // 免密登录状态
  }
}

```

### 5.3 根据 userId 批量查询用户信息接口

**GET** `http://localhost:8080/us/users/list`

**说明**：通过一组用户 ID（userId）批量获取对应的详细用户信息。该接口支持多 ID 查询，常用于列表页显示发布者信息、管理员批量审核用户等场景。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |
| ids | Query | Array<Long> | 是 | 用户 ID 集合（支持多个 id 参数或逗号分隔） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707159300000, 
  "data": [          // 用户详细信息列表
    {
      "id": 2004151376778743810,   // 主键ID (雪花算法)
      "username": "张三",           // 用户昵称
      "email": "zhangsan@edu.cn",  // 用户邮箱
      "phone": "13800138000",      // 手机号码
      "avatarUrl": "http://...",   // 头像地址
      "studentId": "20230101",     // 学号
      "school": "安阳师范学院",      // 所属学校
      "department": "计算机学院",    // 所属院系
      "grade": "2023级",            // 年级
      "creditScore": 100,          // 信用分数
      "isVerified": true,          // 是否实名认证
      "status": 1,                 // 账号状态：1-正常 2-禁用
      "lastLoginAt": 1707159000000,// 最后登录时间
      "realName": "张*",            // 真实姓名
      "gender": 1,                 // 性别：1-男 2-女 0-保密
      "birthday": "2002-01-01",    // 出生日期
      "bio": "发现好物，分享快乐",    // 个人简介
      "qq": "123456789",           // QQ号码
      "wechat": "wx_123456",       // 微信号
      "role": "student",           // 角色名称
      "createdAt": 1707150000000,  // 创建时间
      "updatedAt": 1707159000000   // 更新时间
    }
  ]
}

```

### 5.4 分页查询用户列表接口

**GET** `http://localhost:8080/us/users/page`

**说明**：管理端分页查询用户列表，支持通过关键字（昵称/邮箱/学号等）和账号状态进行筛选，并可自定义排序规则。常用于后台管理系统的用户账号运维与监控。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 默认值 | 字段解释 |
| --- | --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | - | 管理员登录 token |
| pageNo | Query | Integer | 否 | 1 | 页码（最小值：1） |
| pageSize | Query | Integer | 否 | 20 | 每页条数（最小值：1） |
| sortBy | Query | String | 否 | - | 排序字段名称 |
| isAsc | Query | Boolean | 否 | true | 是否升序（true: 升序, false: 降序） |
| keyword | Query | String | 否 | - | 搜索关键字（模糊匹配昵称、邮箱、学号等） |
| status | Query | Integer | 否 | - | 账号状态筛选：1-正常, 2-禁用 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707159600000, 
  "data": {
    "total": 125,      // 符合条件的总记录数
    "pages": 7,        // 总页数
    "list": [          // 当前页用户数据
      {
        "id": 2004151376778743810,
        "username": "测试用户01",
        "email": "test01@edu.cn",
        "phone": "13811112222",
        "avatarUrl": "http://...",
        "studentId": "2023001",
        "school": "安阳师范学院",
        "department": "计算机学院",
        "grade": "2023级",
        "creditScore": 100,
        "isVerified": true,
        "status": 1,
        "lastLoginAt": 1707159000000,
        "realName": "张三",
        "gender": 1,
        "birthday": "2002-05-20",
        "bio": "个人简介内容",
        "qq": "123456",
        "wechat": "wx_test",
        "role": "student",
        "createdAt": 1707150000000,
        "updatedAt": 1707159000000
      }
    ]
  }
}

```

### 5.5 根据用户 ID 获取用户信息接口

**GET** `http://localhost:8080/us/users/{userId}`

**说明**：根据用户唯一 ID（userId）获取该用户的完整详细信息。该接口常用于个人信息展示页、用户资料编辑预览等场景。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |
| userId | Path | Long | 是 | 用户唯一标识 ID（雪花算法生成） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707160200000, 
  "data": {          // 用户详细信息
    "id": 2004599137584095234,     // 用户 ID
    "username": "简美用户",         // 用户昵称
    "email": "user@example.com",   // 用户邮箱
    "phone": "13900001111",        // 手机号码
    "avatarUrl": "http://...",      // 头像图片 URL
    "studentId": "20230506",       // 学号
    "school": "安阳师范学院",       // 所属学校
    "department": "美术学院",       // 所属院系
    "grade": "2023级",             // 年级
    "creditScore": 100,            // 信用分数
    "isVerified": true,            // 是否实名认证
    "status": 1,                   // 账号状态：1-正常, 2-禁用
    "lastLoginAt": 1707159800000,  // 最后登录时间
    "realName": "王小明",           // 真实姓名
    "gender": 1,                   // 性别：1-男, 2-女, 0-保密
    "birthday": "2002-10-12",      // 出生日期
    "bio": "热爱生活，发现美好。",   // 个人简介
    "qq": "10001",                 // QQ 号码
    "wechat": "wx_10001",          // 微信号
    "role": "student",             // 角色名称
    "createdAt": 1707100000000,    // 创建时间
    "updatedAt": 1707160000000     // 更新时间
  }
}

```

### 5.6 获取用户个人信息接口

**GET** `http://localhost:8080/us/users/me`

**说明**：获取当前登录用户的个人详细资料。系统通过请求头中的 `Authorization` 自动识别当前操作用户。该接口通常用于移动端或 Web 端“个人中心”的数据初始化。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录 token |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707160500000, 
  "data": {          // 当前登录用户详情
    "id": 2004151376778743810, 
    "username": "极简开发者",       // 昵称
    "email": "dev@aynu.edu.cn",    // 邮箱
    "phone": "13800000000",        // 手机号
    "avatarUrl": "http://...",      // 头像
    "studentId": "20230001",       // 学号
    "school": "安阳师范学院",       // 学校
    "department": "软件学院",       // 院系
    "grade": "2023级",             // 年级
    "creditScore": 100,            // 信用分
    "isVerified": true,            // 认证状态
    "status": 1,                   // 账号状态
    "lastLoginAt": 1707160000000, 
    "realName": "王*",             // 实名
    "gender": 1,                   // 性别
    "birthday": "2002-08-08", 
    "bio": "Keep coding, keep sharing.", // 简介
    "qq": "888888", 
    "wechat": "wx_dev", 
    "role": "admin",               // 用户角色
    "createdAt": 1707100000000, 
    "updatedAt": 1707160000000
  }
}

```

### 5.7 更新用户信息接口

**PUT** `http://localhost:8080/us/users/{userId}`

**说明**：根据用户 ID 更新用户的个人资料。用户可以修改自己的昵称、头像、联系方式、个人简介等字段；管理员可进行账号状态维护或信用分调整。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 访问令牌（JWT） |
| userId | Path | Long | 是 | 待更新的用户唯一 ID |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| id | Long | 否 | 主键ID |
| username | String | 否 | 用户昵称 |
| email | String | 否 | 用户邮箱 |
| phone | String | 否 | 手机号码 |
| password | String | 否 | 密码 |
| avatarUrl | String | 否 | 头像图片URL地址 |
| studentId | String | 否 | 学号 |
| school | String | 否 | 所属学校 |
| department | String | 否 | 所属院系 |
| grade | String | 否 | 年级 |
| creditScore | Integer | 否 | 信用分数 |
| isVerified | Boolean | 否 | 是否实名认证 |
| status | Integer | 否 | 账号状态：1-正常 2-禁用 |
| lastLoginAt | Long | 否 | 最后登录时间戳 |
| realName | String | 否 | 真实姓名 |
| gender | Integer | 否 | 性别：1-男 2-女 0-保密 |
| birthday | String | 否 | 出生日期 |
| bio | String | 否 | 个人简介/个性签名 |
| qq | String | 否 | QQ号码 |
| wechat | String | 否 | 微信号 |
| role | String | 否 | 角色名称 |
| type | Integer | 否 | 类型 |
| createdAt | Long | 否 | 创建时间 |
| updatedAt | Long | 否 | 更新时间 |

**Returns**

```json
{
  "code": 0,
  "msg": "更新成功",
  "ts": 1707160800000,
  "data": {
    "id": 2004151376778743810,
    "username": "新昵称",
    "email": "dev@aynu.edu.cn",
    "phone": "13800001111",
    "password": "••••••••",
    "avatarUrl": "http://example.com/avatar.png",
    "studentId": "20230001",
    "school": "安阳师范学院",
    "department": "软件学院",
    "grade": "2023级",
    "creditScore": 100,
    "isVerified": true,
    "status": 1,
    "lastLoginAt": 1707160000000,
    "realName": "王*",
    "gender": 1,
    "birthday": "2002-08-08",
    "bio": "Keep coding, keep sharing.",
    "qq": "888888",
    "wechat": "wx_dev",
    "role": "admin",
    "type": 1,
    "createdAt": 1707100000000,
    "updatedAt": 1707160800000
  }
}

```

### 5.8 获取用户统计信息接口

**GET** `http://localhost:8080/us/users/{userId}/stats`

**说明**：获取指定用户的业务统计数据，包括发布、借用、借出的物品数量以及信用评价汇总。该接口常用于用户个人主页的数字化成果展示。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 访问令牌（JWT） |
| userId | Path | Long | 是 | 关联的用户唯一 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "查询成功",  // 提示信息
  "ts": 1707161200000, 
  "data": {          // 用户统计信息 VO
    "id": 5001,                  // 统计记录主键 ID
    "userId": 2004151376778743810, // 关联的用户 ID
    "itemsPublished": 12,        // 发布物品数量
    "itemsBorrowed": 8,          // 借用物品数量
    "itemsLent": 5,              // 借出物品数量
    "totalRatings": 15,          // 累计被评价次数
    "averageRating": 4.85,       // 平均评分（保留2位小数）
    "createdAt": 1707100000000,  // 记录创建时间戳
    "updatedAt": 1707161200000   // 记录更新时间戳
  }
}

```

### 5.9 修改密码接口

**POST** `http://localhost:8080/us/users/change-password`

**说明**：用户处于登录状态时，通过验证原始密码来设置新的登录密码。修改成功后，建议客户端清除旧的 Token 并引导用户重新登录以确保安全性。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 访问令牌（JWT） |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| oldPassword | String | 是 | 用户的当前原始密码 |
| newPassword | String | 是 | 准备设置的新密码（6-20位） |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "密码修改成功", // 提示信息
  "ts": 1707161500000,
  "data": {}         // 无具体返回数据
}

```

### 5.10 实名认证接口

**POST** `http://localhost:8080/us/users/verify`

**说明**：用户提交真实姓名与身份证号码进行实名身份校验。认证成功后，用户账户的 `isVerified` 状态将变更为 `TRUE`。该操作通常是用户参与借用、发布贵重物品等核心业务的前提条件。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 访问令牌（JWT） |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| realName | String | 是 | 用户的真实姓名（最大 50 字符） |
| idCard | String | 是 | 18 位二代身份证号码 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "实名认证成功", 
  "ts": 1707161800000,
  "data": {}         // 无具体返回数据
}

```

### 5.11 修改用户统计数据接口

**PUT** `http://localhost:8080/us/users/{userId}/stats`

**说明**：根据指定的统计类型枚举值（statsEnum），对用户的统计数据进行增量更新或修正。该接口通常由系统内部逻辑（如交易完成、评价提交）触发，用于维护用户业务数据的准确性。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 访问令牌（JWT） |
| userId | Path | Long | 是 | 关联的用户唯一 ID |
| statsEnum | Query | String | 是 | 统计项枚举值（见下表） |

**枚举值说明 (statsEnum)**

| 枚举名   | 说明 |
|-------| --- |
| **1** | 发布物品数量 |
| **2** | 借用物品数量 |
| **3** | 借出物品数量 |
| **4** | 累计被评价次数 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "操作成功",  // 提示信息
  "ts": 1707162100000,
  "data": {}         // 无具体返回数据
}

```

## 6.物品管理模块
### 6.1 新增物品信息接口

**POST** `http://localhost:8080/is/items`

**说明**：发布新的租赁物品。提交物品标题、描述、分类、成色、图片列表、价格及计费规则等信息。发布成功后，物品进入系统并分配唯一 ID。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body**

| 字段名 | 字段类型          | 是否必填 | 字段解释 |
| --- |---------------| --- | --- |
| id | Long          | 否 | 物品ID（新增时为 null，更新时必填） |
| title | String        | 是 | 物品标题（最大 100 字符） |
| description | String        | 是 | 物品详细描述 |
| categoryId | Long          | 是 | 分类ID |
| conditionLevel | Integer       | 是 | 物品成色：0-全新、1-九成新等 |
| images | Array[String] | 是 | 图片 URL 列表 |
| price | Number        | 是 | 租赁单价（需大于 0.01） |
| billingType | Integer       | 是 | 计费类型：PER_DAY(天), PER_WEEK(周), PER_MONTH(月) |
| deposit | Number        | 是 | 押金金额（需大于等于 0） |
| isNegotiable | Boolean       | 是 | 是否可议价 |
| minBorrowDays | Integer       | 否 | 最小租赁天数（默认为 1） |
| maxBorrowDays | Integer       | 否 | 最大租赁天数 |
| location | String        | 是 | 校区/区域位置 |
| address | String        | 否 | 详细地址 |
| borrowConditions | String        | 否 | 借用条件限制说明 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "发布成功",  // 提示信息
  "ts": 1707181500000,
  "data": 2004599137584095234 // 返回新创建的物品唯一 ID
}

```

### 6.2 更新物品信息接口

**PUT** `http://localhost:8080/is/items/{id}`

**说明**：根据物品 ID 更新已发布的物品详情。用户可对其名下的物品标题、描述、价格、图片及租赁条件进行修改。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| id | Path | Long | 是 | 待更新的物品唯一 ID |

**Request Body**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| id | Long | 是 | 物品 ID（更新时必填） |
| title | String | 是 | 物品标题（最大 100 字符） |
| description | String | 是 | 物品详细描述 |
| categoryId | Long | 是 | 分类 ID |
| conditionLevel | Integer | 是 | 物品成色：0-全新、1-九成新等 |
| images | Array[String] | 是 | 物品图片 URL 列表 |
| price | Number | 是 | 租赁单价（需大于 0.01） |
| billingType | Integer | 是 | 计费类型 |
| deposit | Number | 是 | 押金金额（需大于等于 0） |
| isNegotiable | Boolean | 是 | 是否可议价 |
| minBorrowDays | Integer | 否 | 最小租赁天数（需大于等于 1） |
| maxBorrowDays | Integer | 否 | 最大租赁天数 |
| location | String | 是 | 校区/区域位置 |
| address | String | 否 | 详细地址 |
| borrowConditions | String | 否 | 借用条件限制说明 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "更新成功",  // 提示信息
  "ts": 1707182000000,
  "data": true       // 返回布尔值，表示更新是否成功
}

```

### 6.3 删除物品信息接口

**DELETE** `http://localhost:8080/is/items/{id}`

**说明**：根据物品 ID 删除指定的物品记录。通常情况下，只有物品的所有者或管理员有权执行此操作。若物品当前处于“借出中”等活跃订单状态，系统可能会限制删除。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| id | Path | Long | 是 | 待删除的物品唯一 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "删除成功",  // 提示信息
  "ts": 1707182500000,
  "data": true       // 返回布尔值，表示删除操作是否执行成功
}

```

### 6.4 根据 ID 获取物品详情接口

**GET** `http://localhost:8080/is/items/{id}`

**说明**：根据物品唯一 ID 查询该物品的完整详细信息。返回结果包含物品的基本描述、价格规则、地理位置、发布者信息（昵称及头像）以及物品当前的实时状态。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| id | Path | Long | 是 | 物品唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1707203600000,
  "data": {
    "id": 2004599137584095234,      // 物品 ID
    "userId": 2004151376778743810,  // 发布者 ID
    "username": "技术部小王",        // 发布者昵称
    "avatar": "http://example.com/a.jpg", // 发布者头像
    "title": "九成新笔记本电脑",      // 物品标题
    "description": "配置充足，运行流畅，带充电器", // 物品详述
    "categoryId": 101,              // 分类 ID
    "categoryName": "数码产品",       // 分类名称
    "conditionLevel": "ALMOST_NEW", // 物品成色 (BRAND_NEW, ALMOST_NEW, GENTLY_USED)
    "images": [                     // 物品图片 URL 集合
      "http://img.com/1.jpg",
      "http://img.com/2.jpg"
    ],
    "price": 15.00,                 // 租赁单价（元）
    "billingType": "PER_DAY",       // 计费类型 (PER_DAY, PER_WEEK, PER_MONTH)
    "deposit": 500.00,              // 押金金额（元）
    "isNegotiable": false,          // 是否可议价
    "minBorrowDays": 1,             // 最小租赁天数
    "maxBorrowDays": 30,            // 最大租赁天数
    "location": "东校区教学楼",       // 物品所在位置
    "address": "东校区 3 号楼 201",   // 详细地址
    "borrowConditions": "仅限校内学生",// 借用条件
    "status": "AVAILABLE",          // 物品状态 (AVAILABLE, BORROWED, OFF_SHELF)
    "viewCount": 128,               // 浏览次数
    "favoriteCount": 12             // 收藏次数
  }
}

```

### 6.5 批量更新物品状态接口

**PUT** `http://localhost:8080/is/items/batch/status`

**说明**：对多个物品的流通状态进行批量修改。主要用于管理员进行违规下架处理，或用户批量操作个人物品的上架与下架。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| ids | Query | Array[Long] | 是 | 待操作的物品 ID 列表 |
| status | Query | Integer | 是 | 目标状态码（见下表） |

**状态码定义 (status)**

| 状态码 | 枚举名 | 说明 |
| --- | --- | --- |
| **1** | **AVAILABLE** | 可借用（上架） |
| **2** | **BORROWED** | 已借出 |
| **3** | **OFF_SHELF** | 已下架 |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "批量更新成功",
  "ts": 1707204500000,
  "data": true       // 返回布尔值，表示操作是否全部执行成功
}

```

### 6.6 根据分类分页查询物品接口

**GET** `http://localhost:8080/is/items`

**说明**：支持根据关键词、分类、价格区间、成色、位置等多种条件组合筛选物品。返回结果支持分页，并可根据指定字段进行排序。适用于首页展示、分类搜索及过滤等场景。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| keyword | Query | String | 否 | 搜索关键词（匹配标题或描述） |
| categoryId | Query | Long | 否 | 分类 ID |
| status | Query | Integer | 否 | 状态（1-可借用, 2-已借出, 3-已下架） |
| minPrice | Query | String | 否 | 最低价格 |
| maxPrice | Query | String | 否 | 最高价格 |
| conditionLevel | Query | Integer | 否 | 成色级别（0-全新, 1-九成新, 2-八成新） |
| isDeposit | Query | Boolean | 否 | 是否有押金 |
| location | Query | String | 否 | 校区/区域位置 |
| pageNo | Query | Integer | 否 | 页码（默认 1） |
| pageSize | Query | Integer | 否 | 每页条数（默认 20） |
| sortBy | Query | String | 否 | 排序字段（如 price, viewCount） |
| isAsc | Query | Boolean | 否 | 是否升序（默认 true） |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1707205000000,
  "data": {
    "total": 125,        // 总记录数
    "pages": 7,          // 总页数
    "list": [            // 当前页物品列表
      {
        "id": 2004599137584095234,
        "userId": 2004151376778743810,
        "username": "校园达人",
        "avatar": "http://example.com/a.jpg",
        "title": "九成新笔记本电脑",
        "description": "高性能办公本，带充电器",
        "categoryId": 101,
        "categoryName": "数码产品",
        "conditionLevel": "ALMOST_NEW",
        "images": ["http://img.com/1.jpg"],
        "price": 15.00,
        "billingType": "PER_DAY",
        "deposit": 500.00,
        "isNegotiable": false,
        "minBorrowDays": 1,
        "maxBorrowDays": 30,
        "location": "东校区",
        "address": "3号楼",
        "borrowConditions": "学生证抵押",
        "status": "AVAILABLE",
        "viewCount": 45,
        "favoriteCount": 5
      }
    ]
  }
}

```

### 6.7 根据用户 ID 获取物品列表接口

**GET** `http://localhost:8080/is/items/user/{userId}`

**说明**：获取指定用户所发布的所有物品列表。该接口常用于用户个人主页展示，或者用户在个人中心管理自己发布的闲置物品。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| userId | Path | Long | 是 | 目标用户的唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1707206000000,
  "data": [
    {
      "id": 2004599137584095234,
      "userId": 2004151376778743810,
      "username": "技术部小王",
      "avatar": "http://example.com/avatar.jpg",
      "title": "九成新笔记本电脑",
      "description": "配置充足，运行流畅",
      "categoryId": 101,
      "categoryName": "数码产品",
      "conditionLevel": "ALMOST_NEW",
      "images": ["http://img.com/1.jpg"],
      "price": 15.00,
      "billingType": "PER_DAY",
      "deposit": 500.00,
      "isNegotiable": false,
      "minBorrowDays": 1,
      "maxBorrowDays": 30,
      "location": "东校区",
      "address": "3号楼201",
      "borrowConditions": "仅限校内学生",
      "status": "AVAILABLE",
      "viewCount": 128,
      "favoriteCount": 12
    }
  ]
}

```

### 6.8 获取“我的”发布物品列表接口

**GET** `http://localhost:8080/is/items/my`

**说明**：获取当前登录用户所发布的所有物品列表。该接口会通过请求头中的身份凭证自动识别用户，并返回其名下所有处于不同状态（可借用、已借出、已下架）的物品，便于用户进行管理。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1707206500000,
  "data": [
    {
      "id": 2004599137584095234,
      "userId": 2004151376778743810,
      "username": "我自己的昵称",
      "avatar": "http://example.com/my_avatar.jpg",
      "title": "九成新笔记本电脑",
      "description": "配置充足，运行流畅",
      "categoryId": 101,
      "categoryName": "数码产品",
      "conditionLevel": "ALMOST_NEW",
      "images": ["http://img.com/my_item.jpg"],
      "price": 15.00,
      "billingType": "PER_DAY",
      "deposit": 500.00,
      "isNegotiable": false,
      "minBorrowDays": 1,
      "maxBorrowDays": 30,
      "location": "东校区",
      "address": "3号楼201",
      "borrowConditions": "仅限校内学生",
      "status": "AVAILABLE",
      "viewCount": 256,
      "favoriteCount": 18
    }
  ]
}

```

### 6.9 获取物品统计信息接口

**GET** `http://localhost:8080/is/items/stats`

**说明**：获取系统中物品相关的综合统计数据。通常用于后台管理首页或移动端“发现”页面的数据看板，展示如平台总物品数、今日新增数、当前借出数等关键指标。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1707207000000,
  "data": {
    "totalItems": 1520,      // 系统总物品数
    "availableItems": 1200,  // 当前可借用物品数
    "borrowedItems": 320,    // 当前已借出物品数
    "todayNewItems": 45,     // 今日新增发布数
    "totalViewCount": 85600, // 全站物品总浏览量
    "totalCategoryCount": 12 // 现有物品分类总数
  }
}

```

## 7.CloudFlare R2模块
### 7.1 上传单个文件接口

**POST** `http://localhost:8080/ss/files/upload`

**说明**：将单个文件（如头像、物品图片等）上传至 Cloudflare R2 云存储。上传时需指定业务模块名称，系统将根据模块自动分类存储并返回文件的公开访问 URL。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| module | Query | String | 是 | 业务模块名（如：`avatar`, `items`, `common`） |

**Request Body (multipart/form-data)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| file | File (binary) | 是 | 待上传的文件对象 |

**Returns**

```json
{
  "code": 0,
  "msg": "上传成功",
  "ts": 1707207500000,
  "data": "https://pub-xxxxxx.r2.dev/items/20260206/example_image.png" 
  // 返回上传后的文件访问全路径 URL
}

```

### 7.2 批量上传文件接口

**POST** `http://localhost:8080/ss/files/upload/batch`

**说明**：支持一次性上传多个文件至 Cloudflare R2 云存储。适用于物品详情页多图上传等场景。系统将批量处理文件并按顺序返回对应的公开访问 URL 列表。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| module | Query | String | 是 | 业务模块名（例如：`items`, `feedback`） |

**Request Body (multipart/form-data)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| files | Array[File] | 是 | 待上传的文件对象列表 |

**Returns**

```json
{
  "code": 0,
  "msg": "上传成功",
  "ts": 1738830238000,
  "data": [
    "https://pub-xxxxxx.r2.dev/items/20260206/image_01.png",
    "https://pub-xxxxxx.r2.dev/items/20260206/image_02.png",
    "https://pub-xxxxxx.r2.dev/items/20260206/image_03.png"
  ]
}

```

## 8.通知管理模块
### 8.1 创建或修改全员广播公告接口

**POST** `http://localhost:8080/ns/system_broadcasts/add`

**说明**：用于系统管理员发布全员广播或更新现有公告。当请求体中的 `id` 为空时，系统识别为新建公告；当 `id` 不为空时，则对原有公告内容、类型或发布状态进行修改。该公告通常面向全平台用户展示，用于重要通知、活动宣传或系统维护提醒。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| id | Long | 否 | 公告唯一 ID（修改时必填，新增时留空） |
| title | String | 是 | 公告标题 |
| content | String | 是 | 公告详细正文内容 |
| category | Integer | 是 | 公告类型：1-重要通知(announcement), 2-校园活动(activity), 3-系统维护(maintenance) |
| isActive | Boolean | 否 | 发布状态：true-发布中, false-已撤回/下线（默认为 true） |

**Returns**

```json
{
  "code": 0,
  "msg": "操作成功",
  "ts": 1739106233000,
  "data": {}
}

```

### 8.2 管理员分页获取全员广播公告接口

**GET** `http://localhost:8080/ns/system_broadcasts/list`

**说明**：管理员端专用的公告列表查询接口。支持多维度筛选，包括标题模糊查询、公告类型过滤、状态（有效/失效）过滤以及发布时间的范围筛选。常用于管理后台的公告维护列表。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 默认值 | 字段解释 |
| --- | --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | - | 管理员登录访问令牌 |
| pageNo | Query | Integer | 否 | 1 | 当前页码 |
| pageSize | Query | Integer | 否 | 20 | 每页展示条数 |
| isAsc | Query | Boolean | 否 | true | 是否升序排列 |
| sortBy | Query | String | 否 | - | 排序字段（如 `createdAt`） |
| title | Query | String | 否 | - | 公告标题关键字（模糊搜索） |
| category | Query | Integer | 否 | - | 类型：1-通知, 2-活动, 3-维护 |
| isActive | Query | Boolean | 否 | - | 是否有效：true-发布中, false-已下线 |
| startTime | Query | Long | 否 | - | 筛选范围：开始时间戳（毫秒） |
| endTime | Query | Long | 否 | - | 筛选范围：结束时间戳（毫秒） |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739106500000,
  "data": {
    "total": 50,
    "pages": 3,
    "list": [
      {
        "id": 800123456789,
        "title": "系统升级维护公告",
        "content": "我们将于本周日凌晨进行系统升级...",
        "category": 3,
        "isActive": true,
        "createdAt": 1707100000000,
        "updatedAt": 1707100000000
      },
      {
        "id": 800123456790,
        "title": "春季校园摄影大赛",
        "content": "欢迎各位同学踊跃投稿...",
        "category": 2,
        "isActive": false,
        "createdAt": 1707000000000,
        "updatedAt": 1707050000000
      }
    ]
  }
}

```

### 8.3 删除全员广播公告接口

**DELETE** `http://localhost:8080/ns/system_broadcasts/delete`

**说明**：根据公告 ID 物理删除指定的全员广播内容。该操作通常仅限具有高级管理权限的人员执行。删除后，公告将立即从所有客户端消失且无法通过接口恢复。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录访问令牌 |
| id | Query | Long | 是 | 待删除的公告唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "公告已成功删除",
  "ts": 1739106800000,
  "data": {}
}

```

### 8.4 用户获取近三月通知列表接口

**GET** `http://localhost:8080/ns/system_broadcasts/user/list`

**说明**：面向普通用户获取最近三个月内的系统通知列表。该接口会自动过滤无效（`isActive=false`）的公告，并根据当前登录用户的 ID 匹配返回每条通知的已读/未读状态，常用于移动端或网页端的“通知中心”小铃铛页面。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Returns (application/json)**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739106980000,
  "data": [
    {
      "id": 800123456789,
      "title": "春季运动会报名通知",
      "content": "请各位同学于本周五前在系统完成报名...",
      "category": 2,
      "createdAt": 1707100000000,
      "isRead": false
    },
    {
      "id": 800123456788,
      "title": "系统安全升级提示",
      "content": "为了您的账号安全，建议定期修改密码...",
      "category": 1,
      "createdAt": 1706500000000,
      "isRead": true
    }
  ]
}

```

### 8.5 添加公告为已读接口

**POST** `http://localhost:8080/ns/user_broadcast_status/read`

**说明**：将一个或多个系统公告标记为已读状态。客户端通过在请求体中传入公告 ID 数组，实现批量或单个已读操作。标记后，该用户在调用通知列表接口时，对应的 `isRead` 字段将变为 `true`。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| - | Array<Long> | 是 | 待标记为已读的公告 ID 列表（例如：`[800123, 800124]`） |

**Returns**

```json
{
  "code": 0,
  "msg": "标记已读成功",
  "ts": 1739107120000,
  "data": {}
}

```

### 8.6 查看公告详情接口

**GET** `http://localhost:8080/ns/system_broadcasts/detail`

**说明**：根据公告 ID 获取全员广播公告的完整详细信息。该接口返回公告的标题、正文内容、分类类型、当前生效状态以及创建和更新的时间戳。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 否 | 用户登录访问令牌 |
| id | Query | Long | 是 | 全员广播公告内容 ID |

**Returns (application/json)**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739155651000,
  "data": {
    "id": 800123456789,
    "title": "关于学生宿舍网络升级的通知",
    "content": "为了提供更优质的校园网络服务，学校将于本周五凌晨 2:00 至 4:00 对宿舍区骨干网络进行升级...",
    "category": 3,
    "isActive": true,
    "createdAt": 1707100000000,
    "updatedAt": 1707150000000
  }
}

```

## 9.评价管理模块
### 9.1 创建评价接口

**POST** `http://localhost:8080/rs/reviews/reviews`

**说明**：在借用流程完成后，用户可以对本次交易进行评价。评价包含评分、文字描述及可选的配图，支持匿名发布。创建成功后，系统通常会同步更新被评价人的信用分或物品的综合评分。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| itemId | Long | 是 | 关联的物品 ID |
| targetUserId | Long | 是 | 被评价人（如物品持有者或借用者）ID |
| orderId | Long | 是 | 关联的订单 ID |
| rating | Integer | 是 | 评分等级（1-5 分） |
| content | String | 否 | 详细评价内容 |
| images | Array[String] | 否 | 评价配图 URL 列表 |
| isAnonymous | Boolean | 否 | 是否匿名评价（默认 false） |

**Returns**

```json
{
  "code": 0,
  "msg": "评价成功",
  "ts": 1738830884000,
  "data": 500612345678    // 返回新生成的评价 ID
}

```

### 9.2 获取物品评价列表接口

**GET** `http://localhost:8080/rs/reviews/item/{itemId}`

**说明**：分页获取指定物品下的所有评价记录。该接口常用于物品详情页，展示其他用户对该物品及交易过程的真实反馈，帮助潜在借用者做出决策。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| itemId | Path | Long | 是 | 物品唯一 ID |
| pageNo | Query | Integer | 否 | 页码（默认 1） |
| pageSize | Query | Integer | 否 | 每页条数（默认 20） |
| sortBy | Query | String | 否 | 排序字段（如 `createdAt`） |
| isAsc | Query | Boolean | 否 | 是否升序（默认 true） |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738830919000,
  "data": {
    "total": 15,
    "pages": 1,
    "list": [
      {
        "id": 500612345678,
        "itemId": 2004599137584095234,
        "itemTitle": "九成新笔记本电脑",
        "reviewerId": 2004151376778743905,
        "reviewerName": "李同学",
        "reviewerAvatar": "http://img.com/avatar3.png",
        "targetUserId": 2004151376778743810,
        "targetUserName": "技术部小王",
        "targetUserAvatar": "http://img.com/avatar1.png",
        "orderId": 4005123456,
        "rating": 5,
        "content": "电脑成色很好，卖家学长人非常 nice，推荐借用！",
        "images": [
          "http://img.com/review_1.jpg"
        ],
        "status": 1,
        "isAnonymous": false,
        "createdAt": 1707208500000,
        "updatedAt": 1707208500000
      }
    ]
  }
}

```

### 9.3 获取用户收到的评价列表接口

**GET** `http://localhost:8080/rs/reviews/user/{userId}`

**说明**：分页获取指定用户收到的所有评价记录。该接口通常用于个人主页或信用评价页面，展示他人对该用户的评价历史，是用户信用体系的重要组成部分。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| userId | Path | Long | 是 | 被查询的用户 ID |
| pageNo | Query | Integer | 否 | 页码（默认 1） |
| pageSize | Query | Integer | 否 | 每页条数（默认 20） |
| sortBy | Query | String | 否 | 排序字段（如 `createdAt`） |
| isAsc | Query | Boolean | 否 | 是否升序（默认 true） |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831045000,
  "data": {
    "total": 25,
    "pages": 2,
    "list": [
      {
        "id": 500612345680,
        "itemId": 2004599137584095555,
        "itemTitle": "运动型无人机",
        "reviewerId": 2004151376778743001,
        "reviewerName": "王同学",
        "reviewerAvatar": "http://img.com/avatar_w.png",
        "targetUserId": 2004151376778743810,
        "targetUserName": "当前被查询用户",
        "targetUserAvatar": "http://img.com/avatar_target.png",
        "orderId": 4005123499,
        "rating": 5,
        "content": "租借过程很愉快，物品保护得很好。",
        "images": [],
        "status": 1,
        "isAnonymous": false,
        "createdAt": 1707208800000,
        "updatedAt": 1707208800000
      }
    ]
  }
}

```

### 9.4 获取我发出的评价列表接口

**GET** `http://localhost:8080/rs/reviews/my-reviews`

**说明**：分页获取当前登录用户作为评价人所发出的所有评价记录。用户可以在个人中心的“评价管理”中查看、回顾或管理自己对他人的评价历史。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| pageNo | Query | Integer | 否 | 页码（默认 1） |
| pageSize | Query | Integer | 否 | 每页条数（默认 20） |
| sortBy | Query | String | 否 | 排序字段（如 `createdAt`） |
| isAsc | Query | Boolean | 否 | 是否升序（默认 true） |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831060000,
  "data": {
    "total": 12,
    "pages": 1,
    "list": [
      {
        "id": 500612345690,
        "itemId": 2004599137584095888,
        "itemTitle": "手持云台稳定器",
        "reviewerId": 2004151376778743810,
        "reviewerName": "当前登录用户",
        "reviewerAvatar": "http://img.com/my_avatar.png",
        "targetUserId": 2004151376778743999,
        "targetUserName": "出借人小张",
        "targetUserAvatar": "http://img.com/avatar_xz.png",
        "orderId": 4005123500,
        "rating": 5,
        "content": "非常好用的设备，拍摄很稳定，感谢！",
        "images": [
          "http://img.com/my_review_pic1.jpg"
        ],
        "status": 1,
        "isAnonymous": false,
        "createdAt": 1707209100000,
        "updatedAt": 1707209100000
      }
    ]
  }
}

```

### 9.5 更新评价接口

**PUT** `http://localhost:8080/rs/reviews/reviews/{reviewId}`

**说明**：允许评价人对其已发布的评价进行修改。用户可以更新评分、修改文字内容、重新上传配图或更改是否匿名的状态。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| reviewId | Path | Long | 是 | 评价记录唯一 ID |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| rating | Integer | 否 | 评分等级（1-5 分） |
| content | String | 否 | 修改后的评价内容 |
| images | Array[String] | 否 | 更新后的配图 URL 列表 |
| isAnonymous | Boolean | 否 | 是否匿名评价 |

**Returns**

```json
{
  "code": 0,
  "msg": "更新成功",
  "ts": 1738831120000,
  "data": {
    "id": 500612345690,
    "itemId": 2004599137584095888,
    "itemTitle": "手持云台稳定器",
    "reviewerId": 2004151376778743810,
    "reviewerName": "当前登录用户",
    "reviewerAvatar": "http://img.com/my_avatar.png",
    "targetUserId": 2004151376778743999,
    "targetUserName": "出借人小张",
    "targetUserAvatar": "http://img.com/avatar_xz.png",
    "orderId": 4005123500,
    "rating": 4,
    "content": "追加：使用了一段时间发现手柄处有一点点松动，但总体还是很好用的。",
    "images": [
      "http://img.com/updated_pic.jpg"
    ],
    "status": 1,
    "isAnonymous": false,
    "createdAt": 1707209100000,
    "updatedAt": 1738831120000
  }
}

```

### 9.6 删除评价接口

**DELETE** `http://localhost:8080/rs/reviews/reviews/{reviewId}`

**说明**：根据评价 ID 永久删除指定的评价记录。通常仅允许评价发布者本人或具有管理权限的用户执行此操作。删除后，相关物品或用户的评分可能会重新计算。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| reviewId | Path | Long | 是 | 待删除的评价唯一 ID |

**Returns**

```json
{
  "code": 0,         // 状态码：0-成功，其他-失败
  "msg": "删除成功",  // 提示信息
  "ts": 1738831150000,
  "data": {}         // 返回空对象
}

```

### 9.7 检查是否可以评价接口

**GET** `http://localhost:8080/rs/reviews/can-review/{orderId}`

**说明**：根据订单 ID 校验当前登录用户是否具备评价权限。系统会检查订单状态（如是否已完成）、该订单是否已评价过以及用户身份，常用于前端在订单列表或详情页动态显示/置灰“去评价”按钮。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| orderId | Path | Long | 是 | 订单唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831205000,
  "data": {
    "canReview": true,    // 是否可以评价：true-可以，false-不可以
    "reason": ""          // 不可评价的原因描述（如：订单未完成、已评价等）
  }
}

```

### 9.8 举报评价接口

**POST** `http://localhost:8080/rs/reviews/reviews/{reviewId}/report`

**说明**：用户如发现评价内容存在违规（如言论攻击、虚假评价、广告等），可通过此接口提交举报申请。举报信息将进入后台人工审核流程。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| reviewId | Path | Long | 是 | 被举报的评价唯一 ID |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| reason | String | 是 | 举报原因（例如：垃圾广告、人身攻击、内容失实） |
| description | String | 否 | 举报详细说明，最多 500 字 |

**Returns**

```json
{
  "code": 0,
  "msg": "举报已提交",
  "ts": 1738831280000,
  "data": true      // 是否提交成功
}

```

### 9.9 获取评价统计接口

**GET** `http://localhost:8080/rs/reviews/reviews/stats/{userId}`

**说明**：获取指定用户的综合评价统计数据。包括平均评分、总评价数以及各星级评价的数量分布。常用于用户个人主页，以图形化方式直观展示用户的信用画像。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| userId | Path | Long | 是 | 目标用户唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831350000,
  "data": {
    "averageRating": 4.8,     // 平均评分
    "totalCount": 120,        // 总评价条数
    "ratingDistribution": {   // 各星级数量分布
      "5": 100,
      "4": 15,
      "3": 3,
      "2": 1,
      "1": 1
    },
    "positiveRate": "95.8%"   // 好评率
  }
}

```

## 10.订单管理模块
### 10.1 创建借用订单接口

**POST** `http://localhost:8080/os/orders`

**说明**：借用者提交借用申请时调用此接口。创建订单后，系统会通知物品持有者进行审批，物品状态将同步变更为“申请中”。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| itemId | Long | 是 | 申请借用的物品 ID |
| borrowDays | Integer | 是 | 预期的借用时长（天） |
| purpose | String | 否 | 借用用途详细说明（最多 500 字） |

**Returns**

```json
{
  "code": 0,
  "msg": "申请成功",
  "ts": 1738831385000,
  "data": 400512345678    // 返回新创建的订单 ID
}

```

### 10.2 获取订单列表接口

**GET** `http://localhost:8080/os/orders`

**说明**：分页获取借用订单列表。支持根据订单状态、物品 ID、借用人或出借人 ID 进行多维度筛选。常用于“我的借入”和“我的借出”页面展示。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| status | Query | Integer | 否 | 订单状态：1-申请中, 2-已确认, 3-借用中, 4-已归还, 5-已取消, 6-已拒绝 |
| itemId | Query | Long | 否 | 关联的物品 ID |
| borrowerId | Query | Long | 否 | 借用人 ID |
| lenderId | Query | Long | 否 | 出借人 ID |
| type | Query | String | 否 | 订单分类 |
| pageNo | Query | Integer | 否 | 页码（默认 1） |
| pageSize | Query | Integer | 否 | 每页条数（默认 20） |
| sortBy | Query | String | 否 | 排序字段 |
| isAsc | Query | Boolean | 否 | 是否升序（默认 true） |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831445000,
  "data": {
    "total": 50,
    "pages": 3,
    "list": [
      {
        "id": 400512345678,
        "itemId": 2004599137584095001,
        "title": "大疆手持稳定器",
        "borrowerId": 2004151376778743001,
        "borrowerName": "借用人小王",
        "borrowerAvatar": "http://img.com/avatar_borrower.png",
        "lenderId": 2004151376778743810,
        "lenderName": "出借人小李",
        "lenderAvatar": "http://img.com/avatar_lender.png",
        "price": 15.00,
        "billingType": "PER_DAY", // 计费类型：PER_DAY, PER_WEEK, PER_MONTH
        "deposit": 200.00,
        "borrowDays": 3,
        "totalAmount": 45.00,
        "purpose": "用于周末社团拍摄活动",
        "status": "BORROWING", // 状态：APPLYING, CONFIRMED, BORROWING, RETURNED, CANCELLED, REJECTED
        "borrowTime": 1707210000000,
        "returnTime": 1707469200000,
        "actualReturnTime": null,
        "cancelReason": null,
        "createdAt": 1707100000000,
        "updatedAt": 1707210000000
      }
    ]
  }
}

```

### 10.3 获取订单详情接口

**GET** `http://localhost:8080/os/orders/{orderId}`

**说明**：根据订单 ID 获取完整的订单详细信息。包含物品基础信息、借贷双方信息、计费详情、订单当前状态以及各阶段的时间戳。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| orderId | Path | Long | 是 | 订单唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831500000,
  "data": {
    "id": 400512345678,
    "itemId": 2004599137584095001,
    "title": "大疆手持稳定器",
    "borrowerId": 2004151376778743001,
    "borrowerName": "借用人小王",
    "borrowerAvatar": "http://img.com/avatar_borrower.png",
    "lenderId": 2004151376778743810,
    "lenderName": "出借人小李",
    "lenderAvatar": "http://img.com/avatar_lender.png",
    "price": 15.00,
    "billingType": "PER_DAY",
    "deposit": 200.00,
    "borrowDays": 3,
    "totalAmount": 45.00,
    "purpose": "用于周末社团拍摄活动",
    "status": "BORROWING",
    "borrowTime": 1707210000000,
    "returnTime": 1707469200000,
    "actualReturnTime": null,
    "cancelReason": null,
    "createdAt": 1707100000000,
    "updatedAt": 1707210000000
  }
}

```

### 10.4 更新订单信息接口

**PUT** `http://localhost:8080/os/orders/{orderId}`

**说明**：根据订单 ID 修改订单的相关信息。该接口通常用于在订单尚未进入锁定状态（如“借用中”或“已完成”）之前，对借用天数、用途说明等字段进行调整。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| orderId | Path | Long | 是 | 待更新的订单唯一 ID |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| borrowDays | Integer | 否 | 借用天数 |
| purpose | String | 否 | 借用用途说明 |
| ... | Object | 否 | 其他需要更新的订单属性键值对 |

**Returns**

```json
{
  "code": 0,
  "msg": "更新成功",
  "ts": 1738831560000,
  "data": true      // 更新是否成功：true-成功，false-失败
}

```

### 10.5 取消订单接口

**PUT** `http://localhost:8080/os/orders/cancel`

**说明**：借用人或出借人在订单达成前可发起取消操作。调用后订单状态将变更为 `CANCELLED`。若订单已进入“借用中”状态，通常不可直接取消，需走归还流程。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| orderId | Long | 是 | 待取消的订单 ID |
| reason | String | 否 | 取消原因说明 |

**Returns**

```json
{
  "code": 0,
  "msg": "订单已取消",
  "ts": 1738831620000,
  "data": true      // 操作是否成功
}

```

### 10.6 确认订单接口

**PUT** `http://localhost:8080/os/orders/confirm`

**说明**：出借人收到借用申请后，通过此接口确认订单。确认后订单状态将由 `APPLYING` 变更为 `CONFIRMED`，标志着借贷双方已达成初步意向，等待物品实际交付。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| orderId | Long | 是 | 待确认的订单 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "订单已确认",
  "ts": 1738831680000,
  "data": true      // 操作是否成功
}

```

### 10.7 拒绝订单接口

**PUT** `http://localhost:8080/os/orders/reject`

**说明**：出借人如果不方便借出物品，可以通过此接口拒绝借用申请。调用后订单状态将变更为 `REJECTED`，系统通常会要求提供拒绝原因以告知借用者。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| orderId | Long | 是 | 待拒绝的订单 ID |
| reason | String | 否 | 拒绝原因说明 |

**Returns**

```json
{
  "code": 0,
  "msg": "已拒绝该申请",
  "ts": 1738831740000,
  "data": true      // 操作是否成功
}

```

### 10.8 开始借用接口

**PUT** `http://localhost:8080/os/orders/borrow`

**说明**：在出借人交付物品给借用人时调用此接口。调用后，订单状态将变更为 `BORROWING`（借用中），并记录当前时间为实际借出时间。此时物品正式进入租赁阶段。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| orderId | Long | 是 | 关联的订单 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "已开始借用",
  "ts": 1738831800000,
  "data": true      // 操作是否成功
}

```

### 10.9 归还物品接口

**PUT** `http://localhost:8080/os/orders/return`

**说明**：当出借人确认收到归还的物品后，通过此接口完成订单。调用后，订单状态将变更为 `RETURNED`（已归还），系统记录实际归还时间戳。此时借用流程正式结束，用户可进入评价环节。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| orderId | Long | 是 | 关联的订单 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "物品已成功归还",
  "ts": 1738831860000,
  "data": true      // 操作是否成功
}

```

### 10.10 根据物品ID查询订单列表接口

**GET** `http://localhost:8080/os/orders/item/{itemId}`

**说明**：获取指定物品的所有历史及当前订单记录。该接口通常用于物品持有者查看特定物品的流转记录，或用于系统后台对特定物品的租赁情况进行追溯。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| itemId | Path | Long | 是 | 物品唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738831920000,
  "data": [
    {
      "id": 400512345678,
      "itemId": 2004599137584095001,
      "title": "大疆手持稳定器",
      "borrowerId": 2004151376778743001,
      "borrowerName": "借用人小王",
      "borrowerAvatar": "http://img.com/avatar_borrower.png",
      "lenderId": 2004151376778743810,
      "lenderName": "出借人小李",
      "lenderAvatar": "http://img.com/avatar_lender.png",
      "price": 15.00,
      "billingType": "PER_DAY",
      "deposit": 200.00,
      "borrowDays": 3,
      "totalAmount": 45.00,
      "purpose": "摄影课作业使用",
      "status": "RETURNED",
      "borrowTime": 1707210000000,
      "returnTime": 1707469200000,
      "actualReturnTime": 1707465600000,
      "cancelReason": null,
      "createdAt": 1707100000000,
      "updatedAt": 1707465600000
    }
  ]
}

```

### 10.11 获取订单操作日志接口

**GET** `http://localhost:8080/os/orders/{orderId}/logs`

**说明**：查询指定订单的全生命周期操作流水。日志按时间顺序记录了从“提交申请”到“归还完成”期间每一个关键节点的操作人、具体行为及备注信息，用于处理订单纠纷或追踪订单状态变更历史。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| orderId | Path | Long | 是 | 订单唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738832100000,
  "data": [
    {
      "id": 1001,
      "orderId": 400512345678,
      "operatorId": 2004151376778743001,
      "action": "提交申请",
      "remark": "借用天数：3天，用途：拍摄社团活动",
      "createdAt": 1707100000000
    },
    {
      "id": 1005,
      "orderId": 400512345678,
      "operatorId": 2004151376778743810,
      "action": "确认订单",
      "remark": "准许借出",
      "createdAt": 1707105000000
    },
    {
      "id": 1012,
      "orderId": 400512345678,
      "operatorId": 2004151376778743810,
      "action": "开始借用",
      "remark": "物品已当面交付，状态良好",
      "createdAt": 1707210000000
    }
  ]
}

```

### 10.12 获取借用统计接口

**GET** `http://localhost:8080/os/orders/stats`

**说明**：获取当前登录用户的综合借用统计数据。包含作为借用者（借入）和作为出借者（借出）的累计订单量、待处理任务数及金额统计，常用于用户中心概览页面的数字化展示。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1738832160000,
  "data": {
    "borrowStats": {          // 借入统计（作为借用者）
      "totalCount": 12,       // 累计借入次数
      "pendingConfirm": 2,    // 待对方确认的申请
      "activeOrders": 1,      // 正在借用中的物品数
      "totalSpent": 450.00    // 累计支出租金
    },
    "lendingStats": {         // 借出统计（作为出借者）
      "totalCount": 25,       // 累计借出次数
      "toProcess": 3,         // 待处理的借用申请
      "lendingOrders": 5,     // 当前处于借出状态的订单
      "totalEarned": 1280.00  // 累计收入租金
    }
  }
}

```

## 11.校园模块

### 11.1 新增或修改话题分类接口

**POST** `http://localhost:8080/cs/categories/add`

**说明**：用于管理话题分类。当请求体中的 `id` 为空时，系统执行“新增”操作；当 `id` 不为空时，系统根据该 ID 执行“修改”操作。主要用于运营人员对社区板块（如学习交流、失物招领等）进行维护。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌（需具备管理员权限） |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| id | Long | 否 | 分类唯一 ID（修改时必填，新增时留空） |
| name | String | 是 | 分类名称（如：二手交易、吐槽建议） |
| description | String | 否 | 分类详细描述 |
| sortOrder | Integer | 否 | 排序权重，数值越大，在前台展示的位置越靠前 |

**Returns**

```json
{
  "code": 0,
  "msg": "操作成功",
  "ts": 1738832220000,
  "data": {} 
}

```

### 11.2 删除话题分类接口

**DELETE** `http://localhost:8080/cs/categories/delete`

**说明**：根据分类 ID 物理删除指定的话题分类。删除前系统通常会检查该分类下是否仍有关联的话题，若存在关联内容，建议先迁移话题或禁用分类，以保证数据完整性。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌（需具备管理员权限） |
| id | Query | Long | 是 | 待删除的话题分类 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "删除成功",
  "ts": 1738832300000,
  "data": {}
}

```

### 11.3 获取分类列表接口

**GET** `http://localhost:8080/cs/categories/list`

**说明**：获取所有启用的话题分类列表。返回结果将根据 `sortOrder` 权重进行降序排列，通常用于移动端首页板块导航、发帖页面的分类选择器等场景。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 否 | 用户登录访问令牌 |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739086573000,
  "data": [
    {
      "id": 1,
      "name": "学习交流",
      "description": "课业研讨、资源共享与学术请教",
      "sortOrder": 100,
      "createTime": 1704067200000
    },
    {
      "id": 2,
      "name": "失物招领",
      "description": "寻找丢失物品或发布招领信息",
      "sortOrder": 90,
      "createTime": 1704067200000
    },
    {
      "id": 3,
      "name": "二手交易",
      "description": "校园好物闲置转让",
      "sortOrder": 80,
      "createTime": 1704067200000
    }
  ]
}

```

### 11.4 新建或修改话题接口

**POST** `http://localhost:8080/cs/topics/addOrUpdate`

**说明**：用于发布新话题或编辑已有话题内容。当 `id` 为空时，系统识别为发布新话题；当 `id` 不为空时，系统将对指定 ID 的话题进行覆盖更新。支持通过分类 ID 将话题关联到具体的讨论板块。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| id | Long | 否 | 话题唯一 ID（修改时必填，新增时留空） |
| categoryId | Long | 是 | 所属分类 ID（如：学习交流板块 ID） |
| title | String | 是 | 话题标题 |
| content | String | 是 | 话题正文详细内容 |

**Returns**

```json
{
  "code": 0,
  "msg": "操作成功",
  "ts": 1739087039000,
  "data": {}
}

```

### 11.5 删除话题接口

**DELETE** `http://localhost:8080/cs/topics/delete`

**说明**：根据话题 ID 物理删除指定的讨论区话题。该操作通常仅限话题发布者本人或具有管理权限的用户执行。删除话题将同步移除该话题下的关联数据（如点赞记录、评论等，具体取决于后端实现）。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| id | Query | Long | 是 | 待删除的话题唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "话题已删除",
  "ts": 1739087150000,
  "data": {}
}

```

### 11.6 分页获取话题列表接口

**GET** `http://localhost:8080/cs/topics/list`

**说明**：分页获取讨论区话题列表。支持通过关键字对标题或内容进行模糊搜索，并支持自定义排序字段。返回结果包含发帖人信息、所属分类及互动统计数据。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 默认值 | 字段解释                  |
| --- | --- | --- | --- | --- |-----------------------|
| pageNo | Query | Integer | 否 | 1 | 当前页码                  |
| pageSize | Query | Integer | 否 | 20 | 每页展示条数                |
| sortBy | Query | String | 否 | - | 排序字段（如 `create_time`） |
| isAsc | Query | Boolean | 否 | true | 是否升序排列                |
| keyword | Query | String | 否 | - | 模糊搜索关键字               |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739087220000,
  "data": {
    "total": 120,
    "pages": 6,
    "list": [
      {
        "id": 500123456,
        "categoryId": 1,
        "categoryName": "学习交流",
        "userId": 2004151376778,
        "userAvatar": "http://img.com/avatar1.png",
        "userNickname": "学霸小王",
        "title": "关于考研数学的复习建议",
        "content": "大家在复习全书时一定要注意基础概念...",
        "viewCount": 1520,
        "commentCount": 45,
        "createTime": 1707100000000,
        "updateTime": 1707200000000
      }
    ]
  }
}

```

### 11.7 新增评论接口

**POST** `http://localhost:8080/cs/comments`

**说明**：用户针对特定话题发表评论。调用此接口需提供话题 ID 及评论文本内容。成功发表后，话题的评论总数将相应增加。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| topicId | Long | 是 | 关联的话题唯一 ID |
| content | String | 是 | 评论文本内容 |

**Returns**

```json
{
  "code": 0,
  "msg": "评论发表成功",
  "ts": 1739087760000,
  "data": {}
}

```

### 11.8 删除评论接口

**DELETE** `http://localhost:8080/cs/comments/delete`

**说明**：根据评论 ID 物理删除指定的评论内容。此操作通常仅允许评论发布者本人、话题作者或管理员执行。删除后，对应话题的评论计数将自动扣减。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 用户登录访问令牌 |
| id | Query | Long | 是 | 待删除的评论唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "评论已删除",
  "ts": 1739087820000,
  "data": {}
}

```

### 11.9 分页获取话题评论接口

**GET** `http://localhost:8080/cs/comments/page`

**说明**：根据话题 ID 分页获取其关联的评论列表。返回结果包含评论主体内容、发布者信息（昵称、头像）以及精确到毫秒的发布时间。支持通过分页参数控制数据量，并可自定义排序规则。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 默认值 | 字段解释                  |
| --- | --- | --- | --- | --- |-----------------------|
| topicId | Query | Long | 是 | - | 所属话题的唯一 ID            |
| pageNo | Query | Integer | 否 | 1 | 当前页码（从 1 开始）          |
| pageSize | Query | Integer | 否 | 20 | 每页展示的记录条数             |
| sortBy | Query | String | 否 | - | 排序字段（如 `create_time`） |
| isAsc | Query | Boolean | 否 | true | 是否升序排列                |

**Returns (application/json)**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739087703000,
  "data": {
    "total": 45,
    "pages": 3,
    "list": [
      {
        "id": 600123456789,
        "topicId": 500123456,
        "userId": 2004151376778,
        "username": "校园助手",
        "avatar": "http://img.com/avatar/u123.jpg",
        "content": "这个建议非常中肯，大家可以参考一下。",
        "createTime": 1707210000000
      }
    ]
  }
}

```

### 11.10 根据ID获取话题详情接口

**GET** `http://localhost:8080/cs/topics/getTopicDetail`

**说明**：获取指定话题的完整详细信息。该接口通常用于话题详情页的展示，返回数据包含话题正文、发布者详情（昵称及头像）、所属分类信息以及实时的浏览量和评论量统计。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 否 | 用户登录访问令牌 |
| id | Query | Long | 是 | 话题唯一 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739088960000,
  "data": {
    "id": 500123456789,
    "categoryId": 1,
    "categoryName": "学习交流",
    "userId": 2004151376778,
    "userAvatar": "http://img.com/avatar/u123.jpg",
    "userNickname": "校园达人",
    "title": "关于图书馆选座系统的建议",
    "content": "最近发现图书馆选座系统在高频率使用下偶尔会出现卡顿，建议增加预约提醒功能...",
    "viewCount": 2304,
    "commentCount": 18,
    "createTime": 1707100000000,
    "updateTime": 1707200000000
  }
}

```

### 11.11 创建或修改校园公告接口

**POST** `http://localhost:8080/cs/campus_announcements/save`

**说明**：用于发布新校园公告或编辑已有公告内容。当请求体中的 `id` 为空（null）时，系统识别为创建新公告；当 `id` 不为空时，系统将对对应 ID 的公告进行更新。该接口支持直接控制公告的发布状态。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录访问令牌 |

**Request Body (application/json)**

| 字段名 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- |
| id | Long | 否 | 公告主键 ID（修改时必填，新增时留空） |
| title | String | 是 | 公告标题 |
| content | String | 是 | 公告正文内容 |
| isPublished | Boolean | 是 | 发布状态：true-已发布，false-下线 |

**Returns**

```json
{
  "code": 0,
  "msg": "操作成功",
  "ts": 1739175773000,
  "data": {}
}

```

### 11.12 删除校园公告接口

**DELETE** `http://localhost:8080/cs/campus_announcements/delete`

**说明**：根据主键 ID 物理删除指定的校园公告。该操作将不可逆地从数据库中移除公告记录。通常建议在执行删除前进行确认。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | 管理员登录访问令牌 |
| id | Query | Long | 是 | 待删除的校园公告 ID |

**Returns**

```json
{
  "code": 0,
  "msg": "公告删除成功",
  "ts": 1739175820000,
  "data": {}
}

```

### 11.13 分页获取校园公告列表接口

**GET** `http://localhost:8080/cs/campus_announcements/list`

**说明**：分页获取校园公告列表。支持通过关键字模糊搜索标题或内容，并可根据发布状态（已发布/下线）进行筛选。返回结果包含公告的完整内容及创建、更新时间戳。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 默认值 | 字段解释 |
| --- | --- | --- | --- | --- | --- |
| Authorization | Header | String | 是 | - | 用户登录访问令牌 |
| pageNo | Query | Integer | 否 | 1 | 当前页码 |
| pageSize | Query | Integer | 否 | 20 | 每页展示条数 |
| isAsc | Query | Boolean | 否 | true | 是否升序排列 |
| sortBy | Query | String | 否 | - | 排序字段（如 `createTime`） |
| keyword | Query | String | 否 | - | 搜索关键字（模糊匹配标题或内容） |
| isPublished | Query | Boolean | 否 | - | 发布状态筛选：true-已发布，false-下线 |

**Returns (application/json)**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739176140000,
  "data": {
    "total": 25,
    "pages": 2,
    "list": [
      {
        "id": 900123456789,
        "title": "校运会志愿者招募",
        "content": "现招募校运会志愿者 50 名，主要负责场地引导...",
        "isPublished": true,
        "createTime": 1707300000000,
        "updateTime": 1707305000000
      }
    ]
  }
}

```

### 11.14 根据ID获取校园公告详情接口

**GET** `http://localhost:8080/cs/campus_announcements/get`

**说明**：通过公告 ID 获取单条校园公告的完整详细信息。该接口返回公告的标题、正文全文、当前发布状态以及精确的创建与更新时间戳。适用于点击列表进入详情页时的数据加载。

**Parameters**

| 字段名 | 位置 | 字段类型 | 是否必填 | 字段解释 |
| --- | --- | --- | --- | --- |
| Authorization | Header | String | 否 | 用户登录访问令牌 |
| id | Query | Long | 是 | 校园公告唯一 ID |

**Returns (application/json)**

```json
{
  "code": 0,
  "msg": "查询成功",
  "ts": 1739176285000,
  "data": {
    "id": 900123456789,
    "title": "关于国庆节放假安排的通知",
    "content": "根据学校统筹安排，现将国庆节放假时间通知如下：10月1日至7日放假调休，共7天...",
    "isPublished": true,
    "createTime": 1707500000000,
    "updateTime": 1707510000000
  }
}

```