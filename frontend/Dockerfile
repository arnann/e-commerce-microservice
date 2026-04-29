FROM node:22-alpine AS build
ARG APP_NAME
WORKDIR /workspace
COPY package.json package-lock.json ./
COPY frontend ./frontend
RUN npm ci
RUN npm run build -w ${APP_NAME}

FROM nginx:1.27-alpine
ARG APP_NAME
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/${APP_NAME}/dist /usr/share/nginx/html
EXPOSE 80
