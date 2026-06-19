#!/bin/bash
# Simple deployment script for CI/CD pipeline

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Library Management System - AWS Deployment ===${NC}\n"

# Configuration
AWS_REGION=${AWS_REGION:-us-east-1}
BACKEND_REPO=${BACKEND_REPO:-"library-management"}
BACKEND_BRANCH=${BACKEND_BRANCH:-"main"}
S3_BUCKET=${S3_BUCKET:-"librarym-frontend-prod"}
CLOUDFRONT_ID=${CLOUDFRONT_ID:-"ABCDEFG123456"}

# Function to check if command exists
command_exists() {
  command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
  echo -e "${YELLOW}Checking prerequisites...${NC}"
  
  local missing=0
  
  for cmd in aws docker mvn node npm git; do
    if command_exists "$cmd"; then
      echo -e "${GREEN}✓${NC} $cmd found"
    else
      echo -e "${RED}✗${NC} $cmd NOT found"
      missing=$((missing + 1))
    fi
  done
  
  if [ $missing -gt 0 ]; then
    echo -e "${RED}Please install missing dependencies${NC}"
    exit 1
  fi
}

# Build backend
build_backend() {
  echo -e "\n${YELLOW}Building backend...${NC}"
  cd backend
  mvn clean package -DskipTests -q
  echo -e "${GREEN}✓ Backend build completed${NC}"
  cd ..
}

# Build frontend
build_frontend() {
  echo -e "\n${YELLOW}Building frontend...${NC}"
  cd frontend
  npm ci
  VITE_API_URL="https://${BACKEND_DOMAIN}/api" npm run build
  echo -e "${GREEN}✓ Frontend build completed${NC}"
  cd ..
}

# Deploy frontend to S3
deploy_frontend() {
  echo -e "\n${YELLOW}Deploying frontend to S3...${NC}"
  
  aws s3 sync frontend/dist "s3://${S3_BUCKET}/" \
    --region "${AWS_REGION}" \
    --delete \
    --cache-control "public, max-age=300" \
    --exclude "*.html" \
    --exclude "*.json"
  
  aws s3 sync frontend/dist "s3://${S3_BUCKET}/" \
    --region "${AWS_REGION}" \
    --delete \
    --cache-control "public, max-age=0, must-revalidate" \
    --include "*.html" \
    --include "*.json"
  
  echo -e "${GREEN}✓ Frontend deployed to S3${NC}"
}

# Invalidate CloudFront cache
invalidate_cloudfront() {
  echo -e "\n${YELLOW}Invalidating CloudFront cache...${NC}"
  
  aws cloudfront create-invalidation \
    --distribution-id "${CLOUDFRONT_ID}" \
    --paths "/*" \
    --region "${AWS_REGION}"
  
  echo -e "${GREEN}✓ CloudFront cache invalidated${NC}"
}

# Build and push Docker image for backend
build_docker_image() {
  echo -e "\n${YELLOW}Building Docker image for backend...${NC}"
  
  local image_name="librarym-backend:latest"
  docker build -t "${image_name}" -f backend/Dockerfile .
  
  echo -e "${GREEN}✓ Docker image built: ${image_name}${NC}"
}

# Main deployment logic
main() {
  check_prerequisites
  
  read -p "Deploy backend? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    build_backend
  fi
  
  read -p "Deploy frontend? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    build_frontend
    deploy_frontend
    
    read -p "Invalidate CloudFront? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      invalidate_cloudfront
    fi
  fi
  
  read -p "Build Docker image? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    build_docker_image
  fi
  
  echo -e "\n${GREEN}=== Deployment completed successfully ===${NC}\n"
}

main "$@"
