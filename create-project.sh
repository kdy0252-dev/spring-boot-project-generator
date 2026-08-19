#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
    echo "Usage: $(basename "$0") <project-name> <server|agent|batch>"
}

project_name="${1:-}"
preset="${2:-}"

if [[ -z "$project_name" ]]; then
    read -r -p "Project name: " project_name
fi

if [[ -z "$preset" ]]; then
    read -r -p "Project type (server/agent/batch): " preset
fi

if [[ ! "$project_name" =~ ^[A-Za-z][A-Za-z0-9-]*$ ]]; then
    echo "Project name must start with a letter and contain only letters, numbers, and hyphens." >&2
    exit 1
fi

case "$preset" in
    server|agent|batch) ;;
    *)
        usage >&2
        exit 1
        ;;
esac

target_dir="$PWD/$project_name"
if [[ -e "$target_dir" ]]; then
    echo "Target already exists: $target_dir" >&2
    exit 1
fi

package_segment="$(printf '%s' "$project_name" | tr '[:upper:]' '[:lower:]' | tr -d '-')"
base_package="com.example.$package_segment"
package_path="$(printf '%s' "$base_package" | tr '.' '/')"
application_prefix="$(printf '%s' "$project_name" | awk -F- '{for (field = 1; field <= NF; field++) printf toupper(substr($field, 1, 1)) substr($field, 2)}')"
application_class="${application_prefix}Application"

created=false
cleanup_on_error() {
    if [[ "$created" == true && -d "$target_dir" ]]; then
        rm -rf "$target_dir"
    fi
}
trap cleanup_on_error ERR

mkdir -p "$target_dir"
created=true
cp -R "$SCRIPT_DIR/skeleton/common/." "$target_dir"
cp -R "$SCRIPT_DIR/skeleton/presets/$preset/." "$target_dir"

render() {
    source_file="$1"
    target_file="$2"
    sed \
        -e "s|__PROJECT_NAME__|$project_name|g" \
        -e "s|__PRESET__|$preset|g" \
        -e "s|__BASE_PACKAGE__|$base_package|g" \
        -e "s|__PACKAGE_PATH__|$package_path|g" \
        -e "s|__APPLICATION_CLASS__|$application_class|g" \
        "$source_file" > "$target_file"
    rm "$source_file"
}

render "$target_dir/settings.gradle.kts.template" "$target_dir/settings.gradle.kts"
render "$target_dir/README.md.template" "$target_dir/README.md"
for document_template in "$target_dir/docs"/*.template; do
    render "$document_template" "${document_template%.template}"
done

mkdir -p "$target_dir/app/src/main/java/$package_path"
render \
    "$target_dir/app/src/main/java/Application.java.template" \
    "$target_dir/app/src/main/java/$package_path/$application_class.java"
render \
    "$target_dir/app/src/main/resources/application.yml.template" \
    "$target_dir/app/src/main/resources/application.yml"

mkdir -p "$target_dir/app/src/main/java/$package_path/global/annotation"
for annotation_template in InternalService ModuleBridgePort; do
    sed \
        -e "s|__BASE_PACKAGE__|$base_package|g" \
        "$SCRIPT_DIR/skeleton/source/${annotation_template}.java.template" \
        > "$target_dir/app/src/main/java/$package_path/global/annotation/${annotation_template}.java"
done

mkdir -p "$target_dir/app/src/test/java/$package_path"
if [[ "$preset" == batch ]]; then
    architecture_source="$SCRIPT_DIR/skeleton/architecture/batch"
else
    architecture_source="$SCRIPT_DIR/skeleton/architecture/application"
fi

for architecture_test in "$architecture_source"/*.java; do
    target_test="$target_dir/app/src/test/java/$package_path/$(basename "$architecture_test")"
    sed \
        -e "s|__BASE_PACKAGE__|$base_package|g" \
        -e "s|__APPLICATION_CLASS__|$application_class|g" \
        "$architecture_test" > "$target_test"
done

if [[ -f "$target_dir/app/src/test/java/ApplicationTest.java.template" ]]; then
    render \
        "$target_dir/app/src/test/java/ApplicationTest.java.template" \
        "$target_dir/app/src/test/java/$package_path/${application_class}Test.java"
fi

chmod +x "$target_dir/gradlew"
trap - ERR

echo "Created $preset project: $target_dir"
echo "Verify with: cd '$project_name' && ./gradlew :app:build"
