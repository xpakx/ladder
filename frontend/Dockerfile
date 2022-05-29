# Build stage
FROM node:16.15.0-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM nginx:alpine
COPY --from=build app/dist/ladder/ /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
