````skill
---
name: validate-agent-tools
description: Validates and updates tool references in VS Code agent files (.agent.md). Identifies invalid tools (yellow underlines in editor), verifies availability in current VS Code/Copilot tooling, and updates agent configurations to use supported tools.
argument-hint: "[agent-file-path]"
---

# Validate Agent Tools

This skill provides a systematic process for validating and updating tool references in agent frontmatter `tools:` arrays.

## When to Use This Skill

- An agent file has tools marked as invalid (yellow underline)
- Reviewing agent configurations before distribution
- Creating a new agent and confirming tool availability
- Updating agents to newer tool names or capabilities

## Reference Sources

1. [VS Code Agent Tools](https://code.visualstudio.com/docs/copilot/agents/agent-tools)
2. [VS Code Chat Tools Reference](https://code.visualstudio.com/docs/copilot/reference/copilot-vscode-features#_chat-tools)
3. [VS Code Custom Agents](https://code.visualstudio.com/docs/copilot/customization/custom-agents)
4. [VS Code Agent Skills](https://code.visualstudio.com/docs/copilot/customization/agent-skills)

## Known Valid Built-in Tools (as of 2026-03-21)

### Tool Sets (group multiple tools)
| Tool Set | Purpose |
|---|---|
| `search` | File search, text search, codebase search |
| `edit` | File creation, editing, directory creation |
| `runCommands` | Terminal commands + output reading |
| `runTasks` | Workspace tasks + output reading |
| `runNotebooks` | Notebook cell execution |
| `browser` | (Experimental) Integrated browser interaction |

### Individual Tools
| Tool | Purpose |
|---|---|
| `changes` | Source control changes |
| `codebase` | Semantic code search |
| `createFile` | Create a new file |
| `createDirectory` | Create a new directory |
| `editFiles` | Apply edits to files |
| `extensions` | Search VS Code extensions |
| `fetch` | Fetch web page content |
| `fileSearch` | Search files by glob pattern |
| `githubRepo` | Search a GitHub repo |
| `listDirectory` | List directory contents |
| `problems` | Workspace errors and warnings |
| `readFile` | Read file content |
| `runInTerminal` | Run shell command |
| `runSubagent` | Delegate to a subagent |
| `runTask` | Run a workspace task |
| `runTests` | Run unit tests |
| `testFailure` | Get test failure info |
| `terminalLastCommand` | Last terminal command output |
| `terminalSelection` | Terminal selection |
| `textSearch` | Search text in files |
| `todos` | Track implementation progress |
| `usages` | Find references/implementations |
| `VSCodeAPI` | VS Code extension API info |

## Validation Workflow

### Step 1 — Identify invalid tools

Open the agent file and inspect frontmatter:

```yaml
tools:
  - edit
  - runCommands
  - search
```

Any yellow-underlined tool should be validated against the tables above.

### Step 2 — Check availability

For each invalid tool:
1. Check built-in VS Code tools list (tables above)
2. Check community skills for equivalent capabilities
3. Check local `.github/skills/` for custom definitions
4. Check MCP server tools (typically prefixed, e.g. `mcp_*`)

### Step 3 — Choose action

| Situation | Action |
|---|---|
| Tool is valid but parser lagged | Keep as-is, reload editor (`Developer: Reload Window`) |
| Tool was renamed/deprecated | Replace with current equivalent |
| Tool does not exist | Remove and document why |
| Capability needed but no tool exists | Document gap and suggest workflow fallback via skill/instructions |

### Step 4 — Update the agent file

After editing `tools:` ensure:
- Only valid tools remain
- No duplicates
- YAML indentation is consistent (2-space indent for list items)
- Tools are grouped logically (tool sets first, then individual tools)

### Step 5 — Document and verify

- Add a brief note in commit message about tool changes
- Reload editor; confirm yellow underlines are resolved
- Run repository validation/build checks if applicable

## Commit message example

```
docs(agents): validate tools in developer.agent.md

- Removed unavailable tool: filesystem
- Updated renamed tool: terminal -> runCommands
- Verified against VS Code Chat Tools Reference docs
```

## Common notes

| Tool family | Note |
|---|---|
| Built-in VS Code tools | Usually stable; invalid underline often means typo/version mismatch |
| `mcp_*` tools | Require corresponding MCP server to be configured |
| Custom tool names | Must exist in current runtime; otherwise replace with skills/workflow guidance |
| Claude format tools | VS Code maps Claude tool names (Read, Grep, Bash) to VS Code equivalents |

````
