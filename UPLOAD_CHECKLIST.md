# GitHub 网页上传清单

这个文件用于帮助你把毕业设计项目通过 GitHub 网页上传到仓库。

## 一、创建仓库

在 GitHub 创建仓库时建议这样填写：

```text
Repository name: health-system
Description: 我本人大学阶段的毕业设计，人体健康评测与康养系统的设计与实现
Visibility: Public
Add README file: 不勾选
Add .gitignore: 不选择
Choose a license: No license
```

然后点击绿色按钮：

```text
Create repository
```

## 二、上传哪些文件

创建仓库后，点击：

```text
uploading an existing file
```

或者：

```text
Add file -> Upload files
```

打开本地目录：

```text
D:\86195\github_projects\health-system
```

把下面这些内容拖到 GitHub 上传区域：

```text
backend
user-frontend
admin-frontend
database
docs
.gitignore
README.md
UPLOAD_CHECKLIST.md
```

## 三、不要上传哪些文件

不要上传下面这些目录或文件：

```text
node_modules
dist
target
.idea
.npm-cache
.env
*.log
```

原因：

- `node_modules` 是前端依赖，体积很大，别人可以通过 `npm install` 重新安装。
- `dist` 是前端打包产物，不是源码。
- `target` 是后端编译产物，不是源码。
- `.idea` 是 IDEA 本机配置，和项目功能无关。
- `.env` 可能包含本地密码、密钥或私有配置。

## 四、提交信息

文件上传完成后，在页面底部填写提交信息：

```text
init health assessment and healthcare system
```

然后点击：

```text
Commit changes
```

## 五、上传后检查

上传成功后，仓库首页应该能看到：

- README 正常显示。
- 项目截图能够显示。
- `backend`、`user-frontend`、`admin-frontend`、`database`、`docs` 目录都存在。
- `database/schema.sql` 存在。
- 没有 `node_modules`、`target`、`dist`。

## 六、如果 GitHub 网页不能上传文件夹

如果你点击文件夹后只是进入文件夹，而不是上传文件夹，可以使用拖拽方式：

1. 打开资源管理器。
2. 进入 `D:\86195\github_projects\health-system`。
3. 选中需要上传的文件夹和文件。
4. 直接拖到 GitHub 的上传区域。

Chrome 和 Edge 通常支持拖拽上传文件夹。
