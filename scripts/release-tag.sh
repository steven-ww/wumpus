#!/usr/bin/env bash

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

read -r -p "Release version (e.g. 1.2.3 or v1.2.3): " version_input
version_input="${version_input#"${version_input%%[![:space:]]*}"}"
version_input="${version_input%"${version_input##*[![:space:]]}"}"

if [[ -z "$version_input" ]]; then
  echo "No version provided. Aborting."
  exit 1
fi

if [[ "$version_input" == v* ]]; then
  tag="$version_input"
else
  tag="v$version_input"
fi

if [[ ! "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]]; then
  echo "Version '$tag' is not valid. Expected format like v1.2.3 (or 1.2.3)."
  exit 1
fi


if git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
  echo "Tag '$tag' already exists locally. Aborting."
  exit 1
fi

if [[ -n "$(git ls-remote --tags origin "refs/tags/$tag")" ]]; then
  echo "Tag '$tag' already exists on origin. Aborting."
  exit 1
fi

current_branch="$(git rev-parse --abbrev-ref HEAD)"
current_commit="$(git rev-parse --short HEAD)"

echo "About to create and push release tag:"
echo "  Tag:    $tag"
echo "  Branch: $current_branch"
echo "  Commit: $current_commit"
read -r -p "Continue? (y/N): " confirm

if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
  echo "Cancelled."
  exit 0
fi

git tag -a "$tag" -m "Release $tag"
git push origin "$tag"

echo "Release tag '$tag' pushed to origin."
